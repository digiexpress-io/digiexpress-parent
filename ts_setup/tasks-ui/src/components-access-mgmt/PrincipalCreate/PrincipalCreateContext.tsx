import React from 'react';
import { ActorStatus } from 'descriptor-access-mgmt';
import { getInstance as createTabsContext, SingleTabInit, Tab } from 'descriptor-tabbing';

export interface NewPrincipal {
  username: string;
  email: string;
  commitComment: string;
  status: ActorStatus;
  roles: readonly string[];
  permissions: readonly string[];
}

export interface NewPrincipalContextType {
  entity: NewPrincipal;
  setEmail(newEmail: string): void;
  setUsername(newUsername: string): void;
  setCommitComment(newCommitComment: string): void;
  addRole(newRole: string): void;
  removeRole(roleToRemove: string): void;
  addPermission(newPermission: string): void;
  removePermission(permissionToRemove: string): void;
}

const NewPrincipalContext = React.createContext<NewPrincipalContextType>({} as any);

const NewPrincipalProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [entity, setNewPrincipal] = React.useState<NewPrincipal>({
    username: '',
    email: '',
    commitComment: '',
    status: 'IN_FORCE',
    permissions: [],
    roles: []
  });

  const setEmail = React.useCallback((email: string) => setNewPrincipal(previous => Object.freeze({ ...previous, email })), []);
  const setUsername = React.useCallback((username: string) => setNewPrincipal(previous => Object.freeze({ ...previous, username })), []);
  const setCommitComment = React.useCallback((commitComment: string) => setNewPrincipal(previous => Object.freeze({ ...previous, commitComment })), []);

  const addRole = React.useCallback((newRole: string) => setNewPrincipal(previous => {
    if (previous.roles.includes(newRole)) {
      return previous;
    }
    const updatedRoles = [...previous.roles, newRole];
    return Object.freeze({ ...previous, roles: Object.freeze(updatedRoles) });
  }), []);

  const removeRole = React.useCallback((roleToRemove: string) => setNewPrincipal(previous => {
    if (!previous.roles.includes(roleToRemove)) {
      return previous;
    }
    const updatedRoles = [...previous.roles.filter(role => role != roleToRemove)];
    return Object.freeze({ ...previous, roles: Object.freeze(updatedRoles) });
  }), []);

  const addPermission = React.useCallback((newPermission: string) => setNewPrincipal(previous => {
    if (previous.permissions.includes(newPermission)) {
      return previous;
    }
    const updatedPermissions = [...previous.permissions, newPermission];
    return Object.freeze({ ...previous, permissions: Object.freeze(updatedPermissions) });
  }), []);

  const removePermission = React.useCallback((permissionToRemove: string) => setNewPrincipal(previous => {
    if (!previous.permissions.includes(permissionToRemove)) {
      return previous;
    }
    const updatedPermissions = [...previous.permissions.filter(permission => permission != permissionToRemove)];
    return Object.freeze({ ...previous, permissions: Object.freeze(updatedPermissions) })

  }), []);
  const contextValue: NewPrincipalContextType = React.useMemo(() => {
    return {
      entity,
      setEmail,
      setUsername,
      setCommitComment,
      addRole,
      removeRole,
      addPermission,
      removePermission
    }
  }, [entity, setEmail, setUsername, setCommitComment, addRole, removeRole, addPermission, removePermission]);

  return <NewPrincipalContext.Provider value={contextValue}>{children}</NewPrincipalContext.Provider>
}

export function useNewPrincipal(): NewPrincipalContextType {
  const result: NewPrincipalContextType = React.useContext(NewPrincipalContext);
  return result;
}

const TabsContext = createTabsContext<TabTypes, TabState>();
function initAllTabs(): Record<TabTypes, SingleTabInit<TabState>> {
  return {
    principal_permissions: { body: {}, active: false },
    principal_roles: { body: {}, active: true },
  };
}

export type TabTypes = 'principal_permissions' | 'principal_roles';
export interface TabState { }
export function useTabs() {
  const tabbing = TabsContext.hooks.useTabbing();
  const activeTab: Tab<TabTypes, TabState> = tabbing.getActiveTab();
  function setActiveTab(next: TabTypes) {
    tabbing.withTabActivity(next, { disableOthers: true });
  }
  return { activeTab, setActiveTab };
}


export const PrincipalCreateProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (
    <TabsContext.Provider init={initAllTabs()}>
      <NewPrincipalProvider>
        <>{children}</>
      </NewPrincipalProvider>
    </TabsContext.Provider>
  )
}