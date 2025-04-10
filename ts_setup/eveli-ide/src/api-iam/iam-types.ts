export namespace IamApi {

}


export declare namespace IamApi {

  export type UserPermission =
    'WRENCH_VIEW' | 'WRENCH_EDIT' |
    'STENCIL_VIEW' | 'STENCIL_EDIT' |
    'TASK_ALL_VIEW' | 'TASK_ALL_EDIT' | 'TASK_ALL_DELETE' |
    'TASK_GROUP_VIEW' | 'TASK_GROUP_EDIT' |
    'RELEASE_VIEW' | 'RELEASE_EDIT' |
    'DEPLOYMENT_VIEW' | 'DEPLOYMENT_EDIT' |
    'DASHBOARD_VIEW' |
    'FEEDBACK_VIEW' | 'FEEBACK_EDIT' |
    'DIALOB_VIEW' | 'DIALOB_EDIT' |
    'TABLES_V2'

  export interface User {
    userId: string;
    name: string;
    email: string;
    roles: string[];
    authenticated: boolean;
    authorized: boolean;
    permissions: UserPermission[];

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


  export interface UserGroup {
    id: string,
    groupName: string
  }

  export interface GroupMember {
    userName: string
    userEmail: string
  }

  export interface Group {
    name: string
    description: string
  }
}