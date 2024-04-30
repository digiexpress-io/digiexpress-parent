import React from 'react';
import {
  ActorStatus, ChangePrincipalPermissions, ChangePrincipalRoles,
  Permission, Principal, PrincipalId, PrincipalUpdateCommand, Role,
  ChangePrincipalName
} from 'descriptor-access-mgmt';
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

  addRole: (newRole: string) => void;
  removeRole: (roleToRemove: string) => void;
  addPermission: (newPermission: string) => void;
  removePermission: (permissionToRemove: string) => void;

  getCommands: () => PrincipalUpdateCommand[];
}

class CommandBag {
  private _result: PrincipalUpdateCommand[] = [];

  push<T extends PrincipalUpdateCommand>(init: T) {
    this._result.push(init);
    return this;
  }

  build(entity: PrincipalToEdit, start: Principal): PrincipalUpdateCommand[] {
    if (entity.name !== start.name) {
      this.push<ChangePrincipalName>({
        commandType: 'CHANGE_PRINCIPAL_NAME', id: start.id, name: entity.name, comment: '',
      });
    }

    if ([entity.permissions].sort().join(',') !== [start.permissions].sort().join(',')) {
      this.push<ChangePrincipalPermissions>({
        commandType: 'CHANGE_PRINCIPAL_PERMISSIONS', changeType: 'SET_ALL', id: start.id, comment: '', permissions: [...entity.permissions]
      })
    }
    if ([entity.roles].sort().join(',') !== [start.roles].sort().join(',')) {
      this.push<ChangePrincipalRoles>({
        commandType: 'CHANGE_PRINCIPAL_ROLES', changeType: 'SET_ALL', id: start.id, comment: '', roles: [...entity.roles]
      })
    }
    return this._result;
  }
}

function next(init: PrincipalToEdit): Readonly<PrincipalToEdit> { return Object.freeze(init); }

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

  const setName = React.useCallback((name: string) => setEntity(previous => next({ ...previous, name })), []);
  const setEmail = React.useCallback((email: string) => setEntity(previous => next({ ...previous, email })), []);
  const setCommitComment = React.useCallback((commitComment: string) => setEntity(previous => next({ ...previous, commitComment })), []);

  const addRole = React.useCallback((newRole: string) => setEntity(previous => {
    if (previous.roles.includes(newRole)) {
      return previous;
    }
    const updatedRoles = [...previous.roles, newRole];
    return next({ ...previous, roles: Object.freeze(updatedRoles) });
  }), []);


  const removeRole = React.useCallback((roleToRemove: string) => setEntity(previous => {
    if (!previous.roles.includes(roleToRemove)) {
      return previous;
    }
    const updatedRoles = [...previous.roles.filter(role => role !== roleToRemove)];
    return next({ ...previous, roles: Object.freeze(updatedRoles) });
  }), []);

  const addPermission = React.useCallback((newPermission: string) => setEntity(previous => {
    if (previous.permissions.includes(newPermission)) {
      return previous;
    }
    const updatedPermissions = [...previous.permissions, newPermission];
    return next({ ...previous, permissions: Object.freeze(updatedPermissions) });
  }), []);

  const removePermission = React.useCallback((permissionToRemove: string) => setEntity(previous => {
    if (!previous.permissions.includes(permissionToRemove)) {
      return previous;
    }
    const updatedPermissions = [...previous.permissions.filter(permission => permission !== permissionToRemove)];
    return next({ ...previous, permissions: Object.freeze(updatedPermissions) });
  }), []);

  const getCommands: () => PrincipalUpdateCommand[] = React.useCallback(() => new CommandBag().build(entity, principal), [entity, principal]);

  const contextValue: PrincipalToEditContextType = React.useMemo(() => {
    return {
      entity,
      setName,
      setEmail,
      setCommitComment,
      addRole,
      removeRole,
      addPermission,
      removePermission,
      getCommands
    }
  }, [entity, setName, setEmail, setCommitComment, addRole, removeRole, addPermission, removePermission, getCommands]);

  return (<PrincipalEditContext.Provider value={contextValue}>{children}</PrincipalEditContext.Provider>)
}

export function usePrincipalEdit() {
  const result: PrincipalToEditContextType = React.useContext(PrincipalEditContext);
  return result;
}

export function useSorted<T extends Permission | Role>(entity: PrincipalToEdit, items: T[], key: 'permissions' | 'roles') {
  let sortedItems = [...items]
    .sort((a, b) => a.name.toLocaleLowerCase().localeCompare(b.name))
    .sort((item1: T, item2: T) => {
      const item1BelongsToRole = entity[key].includes(item1.name);
      const item2BelongsToRole = entity[key].includes(item2.name);

      if (item1BelongsToRole && !item2BelongsToRole) {
        return -1; // a comes before b
      } else if (!item1BelongsToRole && item2BelongsToRole) {
        return 1; // b comes before a
      } else {
        return 0; // keep the same order
      }
    })

  return { sortedItems };
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
