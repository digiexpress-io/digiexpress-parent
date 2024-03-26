//TODO THIS FILE IS DEPRECATED TYPES


export type PrincipalId = string;
export type RoleId = string;
export type PermissionId = string;

type PrincipalName = string;
type RoleName = string;
type PermissionName = string;


export type ActorStatus = 'ENABLED' | 'DISABLED'; // Actors cannot be deleted -- instead, they are "disabled"

export interface Principal {
  id: PrincipalId;
  name: PrincipalName;
  email: string;
  roles: Role[];
  status: ActorStatus;
}

export interface Permission {
  id: PermissionId;
  name: PermissionName; // example service.resource.verb --> pubsub.subscriptions.consume
  description: string;
  status: ActorStatus;
}

export interface Role {
  id: RoleId;
  name: RoleName;
  description: string;
  permissions: Permission[];
  principals: Principal[];
  status: ActorStatus;
}


