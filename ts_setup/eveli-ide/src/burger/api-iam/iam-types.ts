export namespace IamApi {

}

export declare namespace IamApi {

  export interface User {
    userId: string;
    name: string;
    email: string;
    roles: string[];
    authenticated: boolean;
    authorized: boolean;

    hasRole(...roles: string[]): boolean;
  }

  export interface UserLiveness {
    expiresIn: number;
  }

  export type AuthType = (
    'ANON' | 
    'USER'
  )
  export interface  IamBackendContextType {
    loginUrl: string;
    authType: AuthType;
    user: User;
    getUser: () => Promise<User>
  }
}