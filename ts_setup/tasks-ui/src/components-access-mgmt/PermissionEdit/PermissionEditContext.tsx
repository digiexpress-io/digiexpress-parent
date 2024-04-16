import React from 'react';
import {
  ChangePermissionDescription, ChangePermissionName, ChangePermissionPrincipals,
  Permission, PermissionId, PermissionUpdateCommand
} from 'descriptor-access-mgmt';
import { getInstance as createTabsContext, SingleTabInit, Tab } from 'descriptor-tabbing';


interface EditedPermission {
  id: PermissionId;
  name: string;
  description: string;
  commitComment: string;
  roles: readonly string[]; // name or id of the role
  principals: readonly string[]; // name or id of the principal
}

export interface PermissionEditContextType {
  entity: EditedPermission;
  setName: (newName: string) => void;
  setDescription: (newDescription: string) => void;
  setCommitComment: (newComment: string) => void;
  getCommands: () => PermissionUpdateCommand[];
  addRole: (newRole: string) => void;
  removeRole: (roleToRemove: string) => void;
  addPrincipal: (newPrincipal: string) => void;
  removePrincipal: (principalToRemove: string) => void;
}

class CommandBag {
  private _result: PermissionUpdateCommand[] = [];
  push<T extends PermissionUpdateCommand>(init: T) {
    this._result.push(init);
    return this;
  }

  build(entity: EditedPermission, start: Permission): PermissionUpdateCommand[] {
    if (entity.name !== start.name) {
      this.push<ChangePermissionName>({
        commandType: 'CHANGE_PERMISSION_NAME', id: start.id, name: entity.name, comment: '',
      });
    }
    if (entity.description !== start.description) {
      this.push<ChangePermissionDescription>({
        commandType: 'CHANGE_PERMISSION_DESCRIPTION', id: start.id, description: entity.description, comment: '',
      });
    }
    if ([...entity.principals].sort().join(',') !== start.principals.sort().join(',')) {
      this.push<ChangePermissionPrincipals>({
        commandType: 'CHANGE_PERMISSION_PRINCIPALS', id: start.id, changeType: 'SET_ALL', principals: [...entity.principals], comment: ''
      });
    }
    return this._result;
  }
}

function next(init: EditedPermission): Readonly<EditedPermission> { return Object.freeze(init); }

const PermissionEditContext = React.createContext<PermissionEditContextType>({} as any);

export const EditPermissionProvider: React.FC<{ children: React.ReactNode, permission: Permission }> = ({ children, permission }) => {
  const [entity, setEntity] = React.useState<EditedPermission>({
    id: permission.id,
    name: permission.name,
    description: permission.description,
    commitComment: '',
    principals: [],
    roles: []
  });

  const setName = React.useCallback((name: string) => setEntity(previous => next({ ...previous, name })), []);
  const setDescription = React.useCallback((description: string) => setEntity(previous => next({ ...previous, description })), []);
  const setCommitComment = React.useCallback((commitComment: string) => setEntity(previous => next({ ...previous, commitComment })), []);

  const addRole = React.useCallback((newRole: string) => setEntity(previous => {
    if (previous.roles.includes(newRole)) {
      return previous;
    }
    const updatedRoles = [...previous.roles, newRole];
    return next({ ...previous, roles: Object.freeze(updatedRoles) })
  }), []);

  const removeRole = React.useCallback((roleToRemove: string) => setEntity(previous => {
    if (!previous.roles.includes(roleToRemove)) {
      return previous;
    }
    const updatedRoles = [...previous.roles.filter(role => role !== roleToRemove)];
    return next({ ...previous, roles: Object.freeze(updatedRoles) });
  }), []);

  const addPrincipal = React.useCallback((newPrincipal: string) => setEntity(previous => {
    if (previous.principals.includes(newPrincipal)) {
      return previous;
    }
    const updatedPrincipals = [...previous.principals, newPrincipal];
    return next({ ...previous, principals: Object.freeze(updatedPrincipals) });
  }), []);

  const removePrincipal = React.useCallback((principalToRemove: string) => setEntity(previous => {
    if (!previous.principals.includes(principalToRemove)) {
      return previous;
    }
    const updatedPrincipals = [...previous.principals.filter(principal => principal !== principalToRemove)];
    return next({ ...previous, principals: Object.freeze(updatedPrincipals) });
  }), []);

  const getCommands: () => PermissionUpdateCommand[] = React.useCallback(() => new CommandBag().build(entity, permission), [entity, permission]);

  const contextValue: PermissionEditContextType = React.useMemo(() => {
    return {
      entity,
      setName,
      setDescription,
      setCommitComment,
      addRole,
      removeRole,
      addPrincipal,
      removePrincipal,
      getCommands
    }
  }, [entity, setName, setDescription, setCommitComment, addRole, removeRole, addPrincipal, removePrincipal, getCommands]);

  return (<PermissionEditContext.Provider value={contextValue}>{children}</PermissionEditContext.Provider>);
}

export function usePermissionEdit(): PermissionEditContextType {
  const result: PermissionEditContextType = React.useContext(PermissionEditContext);
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

export const PermissionEditProvider: React.FC<{ children: React.ReactNode, permission: Permission }> = ({ children, permission }) => {
  return (
    <TabsContext.Provider init={initAllTabs()}>
      <EditPermissionProvider permission={permission}>
        {children}
      </EditPermissionProvider>
    </TabsContext.Provider>
  )
}