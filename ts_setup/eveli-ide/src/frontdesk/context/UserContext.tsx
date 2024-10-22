import React, { createContext, PropsWithChildren, useContext } from 'react';
import { useFetch } from '../hooks/useFetch';
import { User } from '../types';


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
  const {response, refresh, } = useFetch<User>(`/userInfo`, {noRetry: true});
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
