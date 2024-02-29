import { Role } from 'descriptor-permissions';
import { getInstance as createTabs } from 'descriptor-tabbing';

export type TabTypes = 'role_create' | 'permission_create';
export const Tabbing = createTabs<TabTypes, {}>();

export interface PermissionsContextType {
  roles: Role[] | undefined;

  loading: boolean; // is permissions loading
  reload(): Promise<void>;
}