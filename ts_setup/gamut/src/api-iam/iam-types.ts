export namespace IamApi {

}

export declare namespace IamApi {


  export interface User {
    token: Token;
    userId: string;
    firstName: string;
    lastName: string;

    representedPerson: RepresentedPerson | undefined;
    representedCompany: RepresentedCompany | undefined;
  }

  export interface UserRoles {
    roles: string[];
    principal: object;
  }

  export interface UserProducts {
    products: string[]; // products that are enabled for the user
  }

  export interface RepresentedPerson { 
    personId: string, 
    name: string 
  }

  export interface RepresentedCompany { 
    companyId: string, 
    name: string 
  }

  export interface Token {
    token: string;
    headerName: string;
  }

  export interface UserLiveness {
    expiresIn: number;
  }

  export type AuthType = (
    'ANON' | 
    'REP_PERSON' | 
    'REP_COMPANY' |
    'USER'
  )

  export type FetchUserGET = () => Promise<Response>;
  export type FetchUserRolesGET = () => Promise<Response>;
  export type FetchUserProductsGET = () => Promise<Response>;
  export type FetchUserLivenessGET = () => Promise<Response>;



  export interface  IamBackendContextType {
    authType: AuthType;

    user: User | undefined;

    userRoles: UserRoles | undefined;
    userProducts: UserProducts | undefined;

    liveness: number | undefined;

    getUser: () => Promise<User | undefined>
  }
}