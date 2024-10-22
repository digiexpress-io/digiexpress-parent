import React from 'react'
import { User } from '../types/User';
import { UserGroup } from '../types/UserGroup';

export interface UserApiConfig {
  apiBaseUrl: string
}

export interface UserBackend {
  getUsers: ()=>Promise<User[]>
  getUserById: (userName: string)=>Promise<User|undefined>
  getGroups:()=>Promise<UserGroup[]>
  backendAvailable: boolean
}

export const UserApiConfigContext = React.createContext<UserApiConfig>({
  apiBaseUrl: ''
});

export const getUsersProto = ()=>Promise.resolve([])
export const getGroupsProto = ()=>Promise.resolve([])
export const getUserByIdProto = (userName: string) => Promise.resolve(undefined);

export const UserBackendProto = {
  getUsers: getUsersProto,
  getUserById: getUserByIdProto,
  getGroups: getGroupsProto,
  backendAvailable: false
}
export const UserBackendContext = React.createContext<UserBackend>(UserBackendProto);
export const UserBackendProvider = UserBackendContext.Provider;
export const UserBackendConsumer = UserBackendContext.Consumer;

export const UserApiContext:React.FC<React.PropsWithChildren<UserApiConfig>> = ({apiBaseUrl, children}) => {

  const getHeaders= ():Headers => {
    const headers = new Headers();
    
    headers.set('Accept', 'application/json');
    headers.set('Content-Type', 'application/json; charset=utf-8');
    return headers;
  }

  const defaultHeaders = getHeaders();

  const getUsers = () => {
    return fetch(`${apiBaseUrl}/api/users`, {headers: defaultHeaders})
      .then(response => response.json());
  }
  const getGroups = () => {
    return fetch(`${apiBaseUrl}/api/group`, {headers: defaultHeaders})
      .then(response => response.json());
  }
  const getUserById = (userId:any) => {
    return fetch(`${apiBaseUrl}/api/users/${userId}`, {headers: defaultHeaders})
    .then(response => response.json());
  }
  

  const apiContext:UserBackend = {
    getUsers: getUsers,
    getUserById: getUserById,
    getGroups: getGroups,
    backendAvailable: true
  }

  return (
    <UserApiConfigContext.Provider value={{apiBaseUrl}}>
      <UserBackendProvider value={apiContext}>
        {children}
      </UserBackendProvider>
    </UserApiConfigContext.Provider>
  )
}