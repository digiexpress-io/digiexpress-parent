import { Permission, Role } from 'descriptor-permissions';
import { getInstance as createTabs } from 'descriptor-tabbing';

export type TabTypes = 'role_create' | 'permission_create' | 'role_parent' | 'role_permissions' | 'role_members';
export const Tabbing = createTabs<TabTypes, {}>();

export interface PermissionsContextType {
  roles: Role[] | undefined;
  permissions: Permission[] | undefined;
  loading: boolean; // is permissions loading
  reload(): Promise<void>;
}