import React from 'react';
import { getInstance as createTabsContext, SingleTabInit, Tab } from 'descriptor-tabbing';


// New role related
export interface NewRole {
  name: string;
  description: string;
  permissions: readonly string[]; // name or id of the permission
  parentId: string | undefined;
  principals: readonly string[]; // name or id of the member
  commitComment: string;
}

export interface NewRoleContextType {
  entity: NewRole;
  setName(newName: string): void;
  setDescription(newDescription: string): void;
  setCommitComment(newCommitComment: string): void;
  setParentId(newParentId: string | undefined): void;
  addPermission(newPermission: string): void;
  removePermission(permissionToRemove: string): void;
  addPrincipal(newPrincipal: string): void;
  removePrincipal(principalToRemove: string): void;
}

const NewRoleContext = React.createContext<NewRoleContextType>({} as any);
const NewRoleProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [entity, setNewRole] = React.useState<NewRole>({
    commitComment: "",
    name: "",
    description: "",
    parentId: undefined,
    principals: [],
    permissions: []
  });

  const setName = React.useCallback((name: string) => setNewRole(previous => Object.freeze({ ...previous, name })), []);
  const setDescription = React.useCallback((description: string) => setNewRole(previous => Object.freeze({ ...previous, description })), []);
  const setCommitComment = React.useCallback((commitComment: string) => setNewRole(previous => Object.freeze({ ...previous, commitComment })), []);
  const setParentId = React.useCallback((parentId: string) => setNewRole(previous => Object.freeze({ ...previous, parentId })), []);

  const addPermission = React.useCallback((newPermission: string) => setNewRole(previous => {
    if (previous.permissions.includes(newPermission)) {
      return previous;
    }
    const updatedPermissions = [...previous.permissions, newPermission];
    return Object.freeze({ ...previous, permissions: Object.freeze(updatedPermissions) });
  }), []);

  const removePermission = React.useCallback((permissionToRemove: string) => setNewRole(previous => {
    if (!previous.permissions.includes(permissionToRemove)) {
      return previous;
    }
    const updatedPermissions = [...previous.permissions.filter((p) => p !== permissionToRemove)];
    return Object.freeze({ ...previous, permissions: Object.freeze(updatedPermissions) });
  }), []);

  const addPrincipal = React.useCallback((newPrincipal: string) => setNewRole(previous => {
    if (previous.principals.includes(newPrincipal)) {
      return previous;
    }
    const updatedPrincipals = [...previous.principals, newPrincipal];
    return Object.freeze({ ...previous, principals: Object.freeze(updatedPrincipals) });
  }), []);

  const removePrincipal = React.useCallback((principalToRemove: string) => setNewRole(previous => {
    if (!previous.principals.includes(principalToRemove)) {
      return previous;
    }
    const updatedPrincipals = [...previous.principals.filter((p) => p !== principalToRemove)];
    return Object.freeze({ ...previous, principals: Object.freeze(updatedPrincipals) });
  }), []);

  const contextValue: NewRoleContextType = React.useMemo(() => {
    return {
      entity,
      setName,
      setDescription,
      setCommitComment,
      setParentId,
      addPermission,
      removePermission,
      addPrincipal,
      removePrincipal
    }
  }, [entity, setName, setDescription, setCommitComment, setParentId, addPermission, removePermission, addPrincipal, removePrincipal]);

  return (<NewRoleContext.Provider value={contextValue}>{children}</NewRoleContext.Provider>);
}

export function useNewRole(): NewRoleContextType {
  const result: NewRoleContextType = React.useContext(NewRoleContext);
  return result;
}


// Tabs related
const TabsContext = createTabsContext<TabTypes, TabState>();
function initAllTabs(): Record<TabTypes, SingleTabInit<TabState>> {
  return {
    role_parent: { body: {}, active: true },
    role_permissions: { body: {}, active: false },
    role_members: { body: {}, active: false }
  };
}

export type TabTypes = 'role_parent' | 'role_permissions' | 'role_members';
export interface TabState { }
export function useTabs() {
  const tabbing = TabsContext.hooks.useTabbing();
  const activeTab: Tab<TabTypes, TabState> = tabbing.getActiveTab();
  function setActiveTab(next: TabTypes) {
    tabbing.withTabActivity(next, { disableOthers: true });
  }
  return { activeTab, setActiveTab };
}

// Root of all
export const RoleCreateProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (
    <TabsContext.Provider init={initAllTabs()}>
      <NewRoleProvider>
        <>{children}</>
      </NewRoleProvider>
    </TabsContext.Provider>
  );
}