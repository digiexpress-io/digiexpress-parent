import React from 'react';
import { IamApi } from './iam-types'

import { IamLiveness } from './IamLiveness'
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from '@tanstack/react-router';
import { useLocale } from '../api-locale';
import { SiteApi } from '../api-site';


export const IamBackendContext = React.createContext<IamApi.IamBackendContextType>({} as any);

export interface IamBackendProviderProps {
  children: React.ReactNode
  liveness: number | undefined;
  staleTime?: number | undefined;
  onExpire: () => void;

  fetchUserGET: IamApi.FetchUserGET
  fetchUserRolesGET: IamApi.FetchUserRolesGET;
  fetchUserProductsGET: IamApi.FetchUserProductsGET;
  fetchUserLivenessGET: IamApi.FetchUserLivenessGET;
}

export const IamBackendProvider: React.FC<IamBackendProviderProps> = (props) => {
  const [userRolesProducts, setUserRolesProducts] = React.useState<{userRoles: IamApi.UserRoles | undefined, userProducts: IamApi.UserProducts | undefined}>();

  const { user, isFirstLoad, reload } = useUser(props);
  const isRolesEnabled = !!user && !!(user.representedCompany || user.representedPerson);

  React.useEffect(() => { 
    if(isRolesEnabled) {

      Promise.all([getUserRoles(props), getUserProducts(props)])
      .then(([userRoles, userProducts]) => {
        setUserRolesProducts({userRoles, userProducts});
      })
    } else {
      setUserRolesProducts(undefined);
    }
  }, [props, user, isRolesEnabled]);

  // create the context
  const contextValue: IamApi.IamBackendContextType = React.useMemo(() => 
    createContext(props, user, userRolesProducts?.userRoles, userRolesProducts?.userProducts, reload),
    [props, user, userRolesProducts, reload]
  );

  if (isFirstLoad) {
    return (<>I'm loading...</>);
  }

  return (<IamBackendContext.Provider value={contextValue}>
    {props.children}
    <IamLiveness fetchUserLivenessGET={props.fetchUserLivenessGET} timeout={props.liveness} onExpire={props.onExpire} user={user}/>
  </IamBackendContext.Provider>);
}

export const useIam = () => {
  return React.useContext(IamBackendContext);
}


function createContext(
  props: IamBackendProviderProps,
  user: IamApi.User | undefined,
  userRoles: IamApi.UserRoles | undefined,
  userProducts: IamApi.UserProducts | undefined,
  reload: () => Promise<IamApi.User | undefined>): IamApi.IamBackendContextType {

  let authType: IamApi.AuthType = 'ANON';
  let userName: string | undefined;
  if(user && user.representedCompany) {
    authType = 'REP_COMPANY';
    userName = user.representedCompany.name;
  } else if(user && user.representedPerson) {
    authType = 'REP_PERSON';
    userName = user.representedPerson.name;
  } else if(user) {
    authType = 'USER'; 
    userName = user.firstName + ' ' + user.lastName;
  }


  const isFormLinkEnabled = (form: SiteApi.TopicLink) => {
    if (form.anon) {
      return true;
    }
    if (authType === 'ANON') {
      return false;
    }
    if (authType === 'USER') {
      return true;
    }
    return (userProducts?.products ?? []).includes(form.value);
  }

  return Object.freeze({
    authType, user, userRoles, userProducts,
    userName, 
    liveness: props.liveness,
    fetchUserGET: props.fetchUserGET,
    getUser: () => reload(),
    reload: async () => {
      const data = await reload();
      return data;
    },
    isFormLinkEnabled,

    getFormLinkAuthType: (link: SiteApi.TopicLink | undefined): IamApi.FormLinkAuthType => {
      const iam = useIam();
      if (!link) {
        return 'IS_FORM_DISABLED';
      }

      const { user, authType } = iam;
      const allowed: boolean = link ? iam.isFormLinkEnabled(link) : false;

      const isAnon = authType === 'ANON';
      if (isAnon) {
        return (allowed ?
          'IS_ANON_FORM_ENABLED' :
          'IS_ANON_FORM_DISABLED');
      }


      const isRep = !!(user?.representedCompany || user?.representedPerson);
      if (isRep) {
        return (allowed ?
          'IS_REP_ENABLED' :
          'IS_REP_DISABLED');
      }

      const isUser = authType === 'USER';
      if (isUser) {
        return (allowed ?
          'IS_USER_FORM_ENABLED' :
          'IS_USER_FORM_DISABLED');
      }

      return 'IS_FORM_DISABLED';
    }
  });
}


function useUser(props: IamBackendProviderProps): {
  user: IamApi.User | undefined,
  isPending: boolean,
  isFirstLoad: boolean
  reload: () => Promise<IamApi.User | undefined>
} {

  const [isFirstLoad, setFirstLoad] = React.useState(true);

  const staleTime = props.staleTime === undefined ? 1000 * 60 : props.staleTime;
  const refetchInterval = staleTime;
  const { data, isPending, refetch, } = useQuery({
    staleTime,
    refetchInterval,
    queryKey: ['iam/user'],
    queryFn: async () => {
      try {
        const resp = await props.fetchUserGET();
        if (!resp.ok) {
          setFirstLoad(false);
          console.log("ANON user", resp.status);
          return null;
        }
        
        const json = await resp.json();
        setFirstLoad(false);
        if(json.type === 'ANON') {
          return null;
        }

        return json.principal;
      } catch (e) {
        setFirstLoad(false);
        console.error("IAM failed!");
        return null;
      }
    }
  });

  const reload = React.useCallback(async () => {
    const result = await refetch();
    return result.data;
  }, [refetch]);

  return {
    user: data ?? undefined,
    isPending,
    isFirstLoad,
    reload
  };
}


async function getUserRoles(props: IamBackendProviderProps): Promise<IamApi.UserRoles | undefined> {
  try {
    const roles = await props.fetchUserRolesGET();
    if(roles.ok) {
      return roles.json();
    }

    console.error("Can't get user roles", { status: roles.status, statusText: roles.statusText });
    return undefined;
  } catch(error) {
    console.error("Can't get user roles", error);
    return undefined;
  }
}

async function getUserProducts(props: IamBackendProviderProps): Promise<IamApi.UserProducts | undefined> {
  try {
    const products = await props.fetchUserProductsGET();
    if(products.ok) {
      return products.json().then(data => {
        const products: IamApi.UserProducts = {
          products: data.allowedProcessNames
        }
        return products;
      });
    }

    console.error("Can't get user products", { status: products.status, statusText: products.statusText });
    return { products: [] };
  } catch(error) {
    console.error("Can't get user products", error);    
    return { products: [] };
  }
}


export function assertAuthenticatedResponse(resp: Response) {
  if(resp.status === 401) {
    throw new UnauthorizedRequestError("Not authorized", resp.status);
  }
}


export function useAssertAuthentication(error: Error | undefined | null) {
  const nav = useNavigate();
  const { locale } = useLocale();
  
  React.useEffect(() => {
    if(error?.name === 'UnauthorizedRequestError') {
      nav({
        params: { locale },
        to: '/public/$locale/login'
      })  
    } 
  }, [error])
}

export class UnauthorizedRequestError extends Error {
  reason: string;
  code: number;
  constructor(reason: string, code: number) {
    super(reason);

    Object.setPrototypeOf(this, UnauthorizedRequestError.prototype);
    this.reason = reason;
    this.code = code;
    this.name = 'UnauthorizedRequestError';
  }
}

