import React from 'react';
import { ActorStatus, Principal, PrincipalId } from 'descriptor-access-mgmt';
import { getInstance as createTabsContext, SingleTabInit, Tab } from 'descriptor-tabbing';


interface PrincipalToEdit {
  id: PrincipalId;
  name: string;
  email: string;
  commitComment: string;
  status: ActorStatus;

  roles: readonly string[];
  permissions: readonly string[];

  directRoles: readonly string[]; //TODO
  directPermissions: readonly string[]; //TODO
}


interface PrincipalToEditContextType {
  entity: PrincipalToEdit;
  setName: (newName: string) => void;
  setEmail: (newEmail: string) => void;
  setCommitComment: (newComment: string) => void;
  setDescription: (newDescription: string) => void;

  addRole: (newRole: string) => void;
  removeRole: (roleToRemove: string) => void;
  addPermission: (newPermission: string) => void;
  removePermission: (permissionToRemove: string) => void;
}

const PrincipalEditContext = React.createContext<PrincipalToEditContextType>({} as any);

export const EditPrincipalProvider: React.FC<{ children: React.ReactNode, principal: Principal }> = ({ children, principal }) => {
  const { id, name, email, roles, permissions, directPermissions, directRoles, status } = principal;

  const [entity, setEntity] = React.useState<PrincipalToEdit>({
    id,
    name,
    email,
    commitComment: '',
    roles,
    permissions,
    status,
    directPermissions,
    directRoles
  });

  const setName = React.useCallback((name: string) => setEntity(previous => Object.freeze({ ...previous, name })), []);
  const setEmail = React.useCallback((email: string) => setEntity(previous => Object.freeze({ ...previous, email })), []);
  const setDescription = React.useCallback((description: string) => setEntity(previous => Object.freeze({ ...previous, description })), []);
  const setCommitComment = React.useCallback((commitComment: string) => setEntity(previous => Object.freeze({ ...previous, commitComment })), []);

  const addRole = React.useCallback((newRole: string) => setEntity(previous => {
    if (previous.roles.includes(newRole)) {
      return previous;
    }
    const updatedRoles = [...previous.roles, newRole];
    return Object.freeze({ ...previous, roles: Object.freeze(updatedRoles) });
  }), []);


  const removeRole = React.useCallback((roleToRemove: string) => setEntity(previous => {
    if (!previous.roles.includes(roleToRemove)) {
      return previous;
    }
    const updatedRoles = [...previous.roles.filter(role => role !== roleToRemove)];
    return Object.freeze({ ...previous, roles: Object.freeze(updatedRoles) });
  }), []);

  const addPermission = React.useCallback((newPermission: string) => setEntity(previous => {
    if (previous.permissions.includes(newPermission)) {
      return previous;
    }
    const updatedPermissions = [...previous.permissions, newPermission];
    return Object.freeze({ ...previous, permissions: Object.freeze(updatedPermissions) });
  }), []);

  const removePermission = React.useCallback((permissionToRemove: string) => setEntity(previous => {
    if (!previous.permissions.includes(permissionToRemove)) {
      return previous;
    }
    const updatedPermissions = [...previous.permissions.filter(permission => permission !== permissionToRemove)];
    return Object.freeze({ ...previous, permissions: Object.freeze(updatedPermissions) });
  }), []);

  const contextValue: PrincipalToEditContextType = React.useMemo(() => {
    return {
      entity,
      setName,
      setEmail,
      setDescription,
      setCommitComment,

      addRole,
      removeRole,
      addPermission,
      removePermission
    }
  }, [entity, setName, setEmail, setDescription, setCommitComment, addRole, removeRole, addPermission, removePermission]);

  return (<PrincipalEditContext.Provider value={contextValue}>{children}</PrincipalEditContext.Provider>)
}

export function usePrincipalEdit() {
  const result: PrincipalToEditContextType = React.useContext(PrincipalEditContext);
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

export const PrincipalEditProvider: React.FC<{ children: React.ReactNode, principal: Principal }> = ({ children, principal }) => {
  return (
    <TabsContext.Provider init={initAllTabs()}>
      <EditPrincipalProvider principal={principal}>
        {children}
      </EditPrincipalProvider>
    </TabsContext.Provider>
  )
}
