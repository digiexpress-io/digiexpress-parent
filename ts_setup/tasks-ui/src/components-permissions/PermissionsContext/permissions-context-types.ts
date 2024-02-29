import { SysConfig } from 'descriptor-sys-config';
import { getInstance as createTabs } from 'descriptor-tabbing';

export type TabTypes = 'role_create' | 'permission_create';
export const Tabbing = createTabs<TabTypes, {}>();

export interface PermissionsContextType {
  permissions: SysConfig | undefined; //TODO SysConfig

  loading: boolean; // is permissions loading
  reload(): Promise<void>;
}