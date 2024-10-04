import React from 'react';
import { IamApi } from './iam-types'

import { IamLiveness } from './IamLiveness'


export const IamBackendContext = React.createContext<IamApi.IamBackendContextType>({} as any);

export interface IamBackendProviderProps {
  children: React.ReactNode
  liveness: number | undefined;
  onExpire: () => void;

  fetchUserGET: IamApi.FetchUserGET
  fetchUserRolesGET: IamApi.FetchUserRolesGET;
  fetchUserProductsGET: IamApi.FetchUserProductsGET;
  fetchUserLivenessGET: IamApi.FetchUserLivenessGET;
}

export const IamBackendProvider: React.FC<IamBackendProviderProps> = (props) => {
  const [user, setUser] = React.useState<IamApi.User>();
  const [userRolesProducts, setUserRolesProducts] = React.useState<{userRoles: IamApi.UserRoles | undefined, userProducts: IamApi.UserProducts | undefined}>();

  // load user and related data
  React.useEffect(() => { getUser(props).then(setUser) }, [props]);
  React.useEffect(() => { 
    if(user) {
      getUserRoles(props).then(async userRoles => {
        const userProducts = userRoles?.roles.length ? await getUserProducts(props) : undefined;
        setUserRolesProducts({userRoles, userProducts});
      });
    } else {
      setUserRolesProducts(undefined);
    }
  }, [props, user]);

  // create the context
  const contextValue: IamApi.IamBackendContextType = React.useMemo(() => 
    createContext(props, user, userRolesProducts?.userRoles, userRolesProducts?.userProducts ), 
    [props, user, userRolesProducts]
  );

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
  userProducts: IamApi.UserProducts | undefined): IamApi.IamBackendContextType {

  let authType: IamApi.AuthType = 'ANON';
  if(user && user.representedCompany) {
    authType = 'REP_COMPANY';
  } else if(user && user.representedPerson) {
    authType = 'REP_PERSON';
  } else {
    authType = 'USER';
  }

  return Object.freeze({
    authType, user, userRoles, userProducts,
    liveness: props.liveness
  });
}

async function getUser(props: IamBackendProviderProps): Promise<IamApi.User | undefined> {
  try {
    const user = await props.fetchUserGET();
    if(user.ok) {
      return user.json();
    }
    return undefined;
  } catch(error) {
    console.log("ANON user");
    return undefined;
  }
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
      return products.json();
    }

    console.error("Can't get user products", { status: products.status, statusText: products.statusText });
    return undefined;
  } catch(error) {
    console.error("Can't get user products", error);
    return undefined;
  }
}
