import { Permission, Principal, Role } from 'descriptor-access-mgmt';


export interface PermissionsContextType {
  roles: Role[];
  permissions: Permission[];
  principals: Principal[];
  loading: boolean; // is permissions loading
  reload(): Promise<void>;
}

