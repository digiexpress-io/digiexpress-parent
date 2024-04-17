import React from 'react';
import {
  ChangeRoleDescription, ChangeRoleName, ChangeRoleParent, ChangeRolePermissions, ChangeRolePrincipals,
  Permission,
  Principal,
  Role, RoleId, RoleUpdateCommand, useAm
} from 'descriptor-access-mgmt';
import { getInstance as createTabsContext, SingleTabInit, Tab } from 'descriptor-tabbing';



interface RoleToEdit {
  id: RoleId;
  name: string;
  parentId: RoleId | undefined;
  description: string;
  commitComment: string;

  principals: readonly string[];
  permissions: readonly string[];
}

interface RoleToEditContextType {
  entity: RoleToEdit;
  setName(newName: string): void;
  setDescription(newDescription: string): void;
  setCommitComment(newComment: string): void;
  setParentId(newParentId: string | undefined): void;

  addPrincipal(newPrincipal: string): void;
  removePrincipal(principalToRemove: string): void;
  addPermission(newPermission: string): void;
  removePermission(permissionToRemove: string): void;

  getCommands: () => RoleUpdateCommand[];
}

class CommandBag {
  private _result: RoleUpdateCommand[] = [];

  push<T extends RoleUpdateCommand>(init: T) {
    this._result.push(init);
    return this;
  }

  build(entity: RoleToEdit, start: Role): RoleUpdateCommand[] {
    if (entity.name !== start.name) {
      this.push<ChangeRoleName>({
        commandType: 'CHANGE_ROLE_NAME', id: start.id, comment: '', name: entity.name,
      });
    }
    if (entity.description !== start.description) {
      this.push<ChangeRoleDescription>({
        commandType: 'CHANGE_ROLE_DESCRIPTION', id: start.id, comment: '', description: entity.description
      });
    }
    if (entity.parentId !== start.parentId) {
      this.push<ChangeRoleParent>({
        commandType: 'CHANGE_ROLE_PARENT', id: start.id, comment: '', parentId: entity.parentId
      });
    }
    if ([...entity.principals].sort().join(',') !== [start.principals].sort().join(',')) {
      this.push<ChangeRolePrincipals>({
        commandType: 'CHANGE_ROLE_PRINCIPALS', id: start.id, changeType: 'SET_ALL', comment: '', principals: [...entity.principals]
      })
    }
    if ([...entity.permissions].sort().join(',') !== [start.permissions].sort().join(',')) {
      this.push<ChangeRolePermissions>({
        commandType: 'CHANGE_ROLE_PERMISSIONS', id: start.id, changeType: 'SET_ALL', comment: '', permissions: [...entity.permissions]
      })
    }
    return this._result;
  }
}

function next(init: RoleToEdit): Readonly<RoleToEdit> { return Object.freeze(init); }

const RoleEditContext = React.createContext<RoleToEditContextType>({} as any);

const EditRoleProvider: React.FC<{ children: React.ReactNode, role: Role }> = ({ children, role }) => {
  const { id, description, name, parentId, permissions, principals } = role;

  const [entity, setEntity] = React.useState<RoleToEdit>({
    id,
    parentId,
    commitComment: '',
    name,
    description,
    permissions,
    principals
  });

  const setName = React.useCallback((name: string) => setEntity(previous => next({ ...previous, name })), []);
  const setDescription = React.useCallback((description: string) => setEntity(previous => next({ ...previous, description })), []);
  const setCommitComment = React.useCallback((commitComment: string) => setEntity(previous => next({ ...previous, commitComment })), []);
  const setParentId = React.useCallback((parentId: RoleId) => setEntity(previous => next({ ...previous, parentId })), []);

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

  const getCommands: () => RoleUpdateCommand[] = React.useCallback(() => new CommandBag().build(entity, role), [entity, role]);

  const contextValue: RoleToEditContextType = React.useMemo(() => {
    return {
      entity,
      setName,
      setDescription,
      setCommitComment,
      setParentId,
      addPrincipal,
      removePrincipal,
      addPermission,
      removePermission,
      getCommands
    }
  }, [entity, setName, setDescription, setCommitComment, setParentId, addPrincipal, removePrincipal, addPermission, removePermission, getCommands]);

  return (<RoleEditContext.Provider value={contextValue}>{children}</RoleEditContext.Provider>)
}

export function useRoleEdit(): RoleToEditContextType {
  const result: RoleToEditContextType = React.useContext(RoleEditContext);
  return result;
}

export function useSorted<T extends Permission | Principal>(entity: RoleToEdit, items: T[], key: 'permissions' | 'principals') {
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

export const RoleEditProvider: React.FC<{ children: React.ReactNode, role: Role }> = ({ children, role }) => {
  return (
    <TabsContext.Provider init={initAllTabs()}>
      <EditRoleProvider role={role}>
        <>{children}</>
      </EditRoleProvider>
    </TabsContext.Provider>
  );
}
