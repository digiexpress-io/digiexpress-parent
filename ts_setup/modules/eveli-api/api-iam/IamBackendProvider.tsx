import React from 'react';
import { IamApi } from './iam-types'
import { IamLiveness } from './IamLiveness'
import { useFetch } from '@dxs-ts/envir-fetch';
import { UserProfileApi } from '@dxs-ts/user-profile';
import { IamForcedLogin } from './IamForcedLogin';
import { useConfig } from '../api-config';
import { SmartTableIntegration } from './SmartTableIntegration';
import { TenantFeature, useTenantConfig } from '../api-tenant-config';


export const IamBackendContext = React.createContext<IamApi.IamBackendContextType>({} as any);

export interface IamBackendProviderProps {
  children: React.ReactNode;
  onExpire?: () => Promise<void>;

}

export const IamBackendProvider: React.FC<IamBackendProviderProps> = (props) => {
  const { loginUrl } = useConfig();

  const { getUser, getEmptyUser } = useFetch('$org/userInfo.GET', {});
  const { restApi } = useFetch('worker/rest/api/userprofiles/$profileId.GET', {});
  const [user, setUser] = React.useState<IamApi.User>(getEmptyUser());
  const [pending, setPending] = React.useState<boolean>(true);

  const { livenessUrl } = useFetch('worker/rest/api/iam/liveness.GET', {});

  const onExpire = React.useCallback(() => {
    if(props.onExpire) {
      return props.onExpire()
    } else {
      return IamForcedLogin({ livenessUrl, loginUrl });
    }
  }, [livenessUrl, loginUrl, props.onExpire]);

  // load user and related data
  React.useLayoutEffect(() => { 
    getUser().then(newUser => {
      setUser(newUser);
      setPending(false);
    })
    .catch(ex => setPending(false))
  }, [props]);
  


  // create the context
  const contextValue: IamApi.IamBackendContextType = React.useMemo(() => {
    const authType: IamApi.AuthType = (user?.authenticated ?? false) ? 'USER' : 'ANON';
    return Object.freeze({authType, user, getUser, loginUrl })
  }, [props, user, loginUrl]);

  if(pending) {
    return (<>I'm loading...</>);
  }

  return (<IamBackendContext.Provider value={contextValue}>
    <>
      {/** link user profile with IAM */}
      <UserProfileApi.Provider backend={restApi} userId={user.userId}>
        <SmartTableIntegration>
          {props.children}
        </SmartTableIntegration>
      </UserProfileApi.Provider>
      <IamLiveness onExpire={onExpire} user={user}/>
      <CreateUserProfile />
    </>
  </IamBackendContext.Provider>);
}


const CreateUserProfile: React.FC<{}> = (props) => {
  const { setUserTenantConfig, features, userTenantConfig } = useTenantConfig();
  const profile = useFetch('worker/rest/api/userprofiles/$profileId.GET', {}); 

  React.useEffect(() => {
    if(userTenantConfig) {
      return;
    }
    const createIfNotDefined = features.includes('user_profile');
    profile.restApi.currentUserProfile(createIfNotDefined)
      .then(profile => {
        console.groupCollapsed('user profile');

        if(!profile) {
          console.log('User profile disabled');
          setUserTenantConfig((prev) => prev ?? []);
          return;
        }
        console.log('Checking user profile', profile);
        setUserTenantConfig(profile.tenantFeatures?.map(e => e as TenantFeature) ?? [])
      })
      .catch((e) => {
        console.log('User profile disabled');
        setUserTenantConfig((prev) => prev ?? []);
      }).finally(() => console.groupEnd());

  }, [setUserTenantConfig, userTenantConfig]);

  return (<></>);
}


export const useIam = () => {
  return React.useContext(IamBackendContext);
}

export const useIamForcedLogin = () => {
  const ctx = React.useContext(IamBackendContext);
  const { livenessUrl } = useFetch('worker/rest/api/iam/liveness.GET', {});

  const loginOn401 = React.useCallback(() => IamForcedLogin({
    livenessUrl, 
    loginUrl: ctx.loginUrl
  }),[ctx.loginUrl, livenessUrl]);

  return { loginOn401 }
}