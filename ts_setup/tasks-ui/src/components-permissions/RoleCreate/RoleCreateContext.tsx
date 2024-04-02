import { getInstance as createTabsContext, SingleTabInit, Tab } from 'descriptor-tabbing';
import { PermissionsProvider } from '../PermissionsContext';

const RoleCreateTabsContext = createTabsContext<RoleCreateTabTypes, RoleCreateTabState>();
function initAllTabs(): Record<RoleCreateTabTypes, SingleTabInit<RoleCreateTabState>> {
  return {
    role_parent: { body: {}, active: true },
    role_permissions: { body: {}, active: false },
    role_members: { body: {}, active: false }
  };
}

export type RoleCreateTabTypes = 'role_parent' | 'role_permissions' | 'role_members';
export interface RoleCreateTabState {
}
export const RoleCreateProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (
    <PermissionsProvider>
      <RoleCreateTabsContext.Provider init={initAllTabs()}>
        <>{children}</>
      </RoleCreateTabsContext.Provider>
    </PermissionsProvider>
  );
}

export function useRoleCreateTabs() {
  const tabbing = RoleCreateTabsContext.hooks.useTabbing();
  const activeTab: Tab<RoleCreateTabTypes, RoleCreateTabState> = tabbing.getActiveTab();
  function setActiveTab(next: RoleCreateTabTypes) {
    tabbing.withTabActivity(next, { disableOthers: true });
  }
  return { activeTab, setActiveTab };
}