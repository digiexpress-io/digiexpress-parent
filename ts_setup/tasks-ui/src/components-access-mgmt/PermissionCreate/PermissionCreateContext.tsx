import React from 'react';
import { getInstance as createTabsContext, SingleTabInit, Tab } from 'descriptor-tabbing';


export interface NewPermission {
  name: string;
  description: string;
  commitComment: string;
  roles: readonly string[]; // name or id of the role
  principals: readonly string[]; // name or id of the principal
}


export interface NewPermissionContextType {
  entity: NewPermission;
  setName(newName: string): void;
  setDescription(newDescription: string): void;
  setCommitComment(newCommitComment: string): void;
  addRole(newRole: string): void;
  removeRole(roleToRemove: string): void;
  addPrincipal(newPrincipal: string): void;
  removePrincipal(principalToRemove: string): void;
}

const NewPermissionContext = React.createContext<NewPermissionContextType>({} as any);

const NewPermissionProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [entity, setNewPermission] = React.useState<NewPermission>({
    name: '',
    description: '',
    commitComment: '',
    roles: [],
    principals: []
  });

  const setName = React.useCallback((name: string) => setNewPermission(previous => Object.freeze({ ...previous, name })), []);
  const setDescription = React.useCallback((description: string) => setNewPermission(previous => Object.freeze({ ...previous, description })), []);
  const setCommitComment = React.useCallback((commitComment: string) => setNewPermission(previous => Object.freeze({ ...previous, commitComment })), []);

  const addRole = React.useCallback((newRole: string) => setNewPermission(previous => {
    if (previous.roles.includes(newRole)) {
      return previous;
    }

    const updatedRoles = [...previous.roles, newRole];
    return Object.freeze({ ...previous, roles: Object.freeze(updatedRoles) })
  }), []);

  const removeRole = React.useCallback((roleToRemove: string) => setNewPermission(previous => {
    if (!previous.roles.includes(roleToRemove)) {
      return previous;
    }

    const updatedRoles = [...previous.roles.filter(role => role !== roleToRemove)];
    return Object.freeze({ ...previous, roles: Object.freeze(updatedRoles) })
  }), []);

  const addPrincipal = React.useCallback((newPrincipal: string) => setNewPermission(previous => {
    if (previous.principals.includes(newPrincipal)) {
      return previous;
    }

    const updatedPrincipals = [...previous.principals, newPrincipal];
    return Object.freeze({ ...previous, principals: Object.freeze(updatedPrincipals) })
  }), []);

  const removePrincipal = React.useCallback((principalToRemove: string) => setNewPermission(previous => {
    if (!previous.principals.includes(principalToRemove)) {
      return previous;
    }

    const updatedPrincipals = [...previous.principals.filter(principal => principal !== principalToRemove)];
    return Object.freeze({ ...previous, principals: Object.freeze(updatedPrincipals) })
  }), []);

  const contextValue: NewPermissionContextType = React.useMemo(() => {
    return {
      entity,
      setName,
      setDescription,
      setCommitComment,
      addRole,
      removeRole,
      addPrincipal,
      removePrincipal
    }
  }, [entity, setName, setDescription, setCommitComment, addRole, removeRole, addPrincipal, removePrincipal]);

  return <NewPermissionContext.Provider value={contextValue}>{children}</NewPermissionContext.Provider>
}

export function useNewPermission(): NewPermissionContextType {
  const result: NewPermissionContextType = React.useContext(NewPermissionContext);
  return result;
}

const TabsContext = createTabsContext<TabTypes, TabState>();
function initAllTabs(): Record<TabTypes, SingleTabInit<TabState>> {
  return {
    permission_roles: { body: {}, active: true },
    permission_members: { body: {}, active: false }
  };
}

export type TabTypes = 'permission_roles' | 'permission_members';
export interface TabState { }
export function useTabs() {
  const tabbing = TabsContext.hooks.useTabbing();
  const activeTab: Tab<TabTypes, TabState> = tabbing.getActiveTab();
  function setActiveTab(next: TabTypes) {
    tabbing.withTabActivity(next, { disableOthers: true });
  }
  return { activeTab, setActiveTab };
}


export const PermissionCreateProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (
    <TabsContext.Provider init={initAllTabs()}>
      <NewPermissionProvider>
        {children}
      </NewPermissionProvider>
    </TabsContext.Provider>
  )
}



