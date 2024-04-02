import { Permission, Principal, Role } from 'descriptor-permissions';


export interface PermissionsContextType {
  roles: Role[];
  permissions: Permission[];
  principals: Principal[];
  loading: boolean; // is permissions loading
  reload(): Promise<void>;
}

