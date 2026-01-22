import { SiteApi } from "../api-site";

export namespace IamApi {

}

export declare namespace IamApi {


  export interface User {
    token: Token;
    userId: string;
    firstName: string;
    lastName: string;
    contact: Contact;
    representedPerson: RepresentedPerson | undefined;
    representedCompany: RepresentedCompany | undefined;
  }

  export interface ContactAddress {
    street: string;
    locality: string;
    postalCode: string;
    country: string;
  }
  export interface Contact {
    email: string;
    address?: ContactAddress;
  }
  export interface UserRoles {
    roles: string[];
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

  export type FormLinkAuthType = (
    'IS_ANON_FORM_ENABLED' | // form can be filled anonymously
    'IS_ANON_FORM_DISABLED' | // form cannot be filled anonymously

    'IS_USER_FORM_ENABLED' | // user is logged in and form is enabled
    'IS_USER_FORM_DISABLED' | // user is logged in but form has error for some reason

    'IS_REP_ENABLED' | // representative is authorized to fill this form
    'IS_REP_DISABLED' | // representative is not authorized to fill this form

    'IS_FORM_DISABLED' // form has error for some reason
  )


  export type FetchUserGET = () => Promise<Response>;
  export type FetchUserRolesGET = () => Promise<Response>;
  export type FetchUserProductsGET = (cockpitId: string | undefined) => Promise<Response>;
  export type FetchUserLivenessGET = () => Promise<Response>;



  export interface  IamBackendContextType {
    authType: AuthType;

    user: User | undefined;

    userName: string | undefined;
    userRoles: UserRoles | undefined;
    userProducts: UserProducts | undefined;

    liveness: number | undefined;

    getUser: () => Promise<User | undefined>;
    fetchUserGET: IamApi.FetchUserGET;
    reload: () => Promise<User | undefined>

    isFormLinkEnabled: (link: SiteApi.TopicLink) => boolean
    getFormLinkAuthType: (link: SiteApi.TopicLink | undefined) => FormLinkAuthType
    onAuthorizationFail: (error: Error, locale: string) => void
  }
}