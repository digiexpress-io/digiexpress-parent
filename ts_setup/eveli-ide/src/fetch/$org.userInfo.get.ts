import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { IamApi } from '@/api-iam';

export const Hook = createFileFetch('$org/userInfo.GET')({
  hook
}) 

interface InternalUserShape {
  userId?: string | undefined;
  email?: string | undefined;
  name?: string | undefined;
  roles?: string[] | undefined;
  authenticated?: boolean | undefined;
  authorized?: boolean | undefined;
}

function toFailSafeUser(json: InternalUserShape | undefined): IamApi.User {
  const roles: string[] = json?.roles ?? [];
  return {
    name: json?.name ?? '',
    userId: json?.userId ?? '',
    email: json?.email ?? '',
    authenticated: json?.authenticated ?? false,
    authorized: json?.authorized ?? false,
    roles,
    // TODO  fix naming
    hasRole: (...roles: string[]) => (roles.filter(role => roles.indexOf(role) > -1).length > 0)
  }
}

function hook(props: {}): {  

  getUser: () => Promise<IamApi.User>,
  getEmptyUser: () => IamApi.User,

} {


  const params = Hook.useNativeParams();
  const { url, method } = params;
  const query = url({ org: '' }).substring(1);

  return { 

    getEmptyUser: () => toFailSafeUser({}),
    getUser: async () => {
      try {
        const response = await window.fetch(query, { method });
        if(!response.ok) {
          return toFailSafeUser({});
        }
        return await response.json().then(toFailSafeUser);
      } catch(error) {
        console.error(`failed to fetch user from: ${query}`, error)
        return toFailSafeUser({});
      }
    }
   }

}


/*
import React, { createContext, PropsWithChildren, useContext } from 'react';
import { User } from '../types';
import { useFetch } from '@dxs-ts/eveli-fetch';


export interface UserContextType {
  user: Partial<User>,
  isAuthenticated: () => boolean;
  isAuthorized: () => boolean;
  hasRole: (...roles: string[]) => boolean;
  refresh: ()=>void;
};

const INITIAL_USER: UserContextType = {
  user: {
    name: '',
    roles: null
  },
  isAuthenticated: () => false,
  isAuthorized: () => false,
  hasRole: (...roles: String[]) => false,
  refresh: ()=>{},
};

export const UserContext = createContext<UserContextType>(INITIAL_USER);

export const UserContextProvider: React.FC<PropsWithChildren> = ({ children }) => {
  const { user: response, refresh, } = useFetch('$org/userInfo.GET', {});
  const user = response || INITIAL_USER.user;
  const isAuthenticated = () =>  !!user.authenticated;
  const isAuthorized = () => !!user.authorized;
  const hasRole = (...roles: string[]) => (!!user?.roles && user.roles.filter(role=> roles.indexOf(role) > -1).length > 0);
  return (
    <UserContext.Provider value={{ user, isAuthenticated, isAuthorized, hasRole, refresh }}>
      {children}
    </UserContext.Provider>
  );
}

export const useUserInfo = () => useContext(UserContext);
*/