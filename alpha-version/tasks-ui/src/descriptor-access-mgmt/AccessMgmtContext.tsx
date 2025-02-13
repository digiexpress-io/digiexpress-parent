import React from 'react';


import { useBackend } from 'descriptor-backend';
import { UserProfileAndOrg } from './profile-types';
import { Principal, Role, RoleId, PrincipalId, Permission } from './permission-types';
import { ImmutableAmStore } from './am-store-impl';
import { UserSearchResult, RoleSearchResult } from './permission-container-types';
import { TenantConfig } from './tenant-types';
import { ImmutablePermissionContainer } from './permission-container-impl';
import { useProfile } from 'descriptor-backend/backend-ctx';

export type { UserSearchResult, RoleSearchResult };
export interface AmContextType {
  userId: PrincipalId
  iam: Principal;
  profile: UserProfileAndOrg;
  roles: Role[];
  principals: Principal[];
  permissions: Permission[];
  
  reload: () => Promise<void>;

  getPermission(idOrNameOrExternalId: string): Permission;
  getPrincipal(idOrNameOrExternalId: string): Principal;
  getRole(idOrNameOrExternalId: string): Role;
  findTaskUsers(searchFor: string, checkedUsers: PrincipalId[]): UserSearchResult[];
  findTaskRoles(searchFor: string, checkedRoles: RoleId[]): RoleSearchResult[];
}


export function useAm() {
  const result = React.useContext(AccessMgmtContext);
  return result;
}

export const AccessMgmtContext = React.createContext<AmContextType>({} as any);

export const AccessMgmtContextProvider: React.FC<{ children: React.ReactNode }> = (props) => {
  const backend = useBackend();
  
  const [data, setData] = React.useState<[
    Permission[],
    Principal[],
    Role[]
  ]>();

  // perform init
  React.useEffect(() => {
    const store = new ImmutableAmStore(backend.store);
    Promise.all([store.findAllPermissions(), store.findAllPrincipals(), store.findAllRoles()])
    .then(setData);
  }, []);

  if(!data) {
    return null;
  }
  const [permissions, principals, roles] = data;

  return (<AccessMgmtContextProviderDelegate
    permissions={permissions}
    principals={principals}
    roles={roles}>
      {props.children}
  </AccessMgmtContextProviderDelegate>)
}


const AccessMgmtContextProviderDelegate: React.FC<{ 
  children: React.ReactNode, 
 
  permissions: Permission[],
  principals: Principal[];
  roles: Role[];
}> 
= (props) => {

  const backend = useBackend();
  const profile = useProfile();

  const [roles, setRoles] = React.useState<Role[]>(props.roles);
  const [permissions, setPermissions] = React.useState<Permission[]>(props.permissions);
  const [principals, setPrincipals] = React.useState<Principal[]>(props.principals);
  const store = React.useMemo(() => new ImmutableAmStore(backend.store), [backend]);

  async function loadAllRoles(): Promise<void> {
    return store.findAllRoles().then(allRoles => {
      if (allRoles.length) {
        setRoles(allRoles);
      }
    });
  }

  async function loadAllPrincipals(): Promise<void> {
    return store.findAllPrincipals().then(allPrincipals => {
      if (allPrincipals.length) {
        setPrincipals(allPrincipals);
      }
    });
  }

  async function loadAllPermissions(): Promise<void> {
    return store.findAllPermissions().then(allPermissions => {
      if (allPermissions.length) {
        setPermissions(allPermissions);
      }
    });
  }

  const contextValue: AmContextType = React.useMemo(() => {
    const container = new ImmutablePermissionContainer(permissions, principals, roles);

    async function reload(): Promise<void> {
      await Promise.all([loadAllRoles(), loadAllPermissions(), loadAllPrincipals()]).then((_values) => {
        console.log('loaded roles and permissions');
      });
    }
    function getPermission(idOrName: string) {
      const result = permissions.find(({ id, name }) => id === idOrName || name === idOrName);
      if (!result) {
        throw new Error("Permission not found by id/name/externalId: " + idOrName);
      }
      return result;
    }
    function getRole(idOrName: string) {
      const result = roles.find(({ id, name }) => id === idOrName || name === idOrName);
      if (!result) {
        throw new Error("Role not found by id/name/externalId: " + idOrName);
      }
      return result;
    }
    function getPrincipal(idOrName: string) {
      const result = principals.find(({ id, name }) => id === idOrName || name === idOrName);
      if (!result) {
        throw new Error("Principal not found by id/name/externalId: " + idOrName);
      }
      return result;
    }
    function findTaskUsers(searchFor: string, checkedUsers: PrincipalId[]): UserSearchResult[] {
      return container.findTaskUsers(searchFor, checkedUsers);
    }
    function findTaskRoles(searchFor: string, checkedRoles: RoleId[]): RoleSearchResult[] {
      return container.findTaskRoles(searchFor, checkedRoles);
    }

    const combinedProfile: UserProfileAndOrg = {
      ...profile,
      all: { roles: toRecord(roles), permissions: toRecord(permissions), principals: toRecord(principals) }
    };

    const iam = getPrincipal(profile.am.principal.id);
    return { 
      userId: profile.am.principal.id,
      loading: false,
      profile: combinedProfile,
      iam, 
      roles, 
      permissions, 
      principals, 
      reload, getPermission, getRole, getPrincipal, findTaskRoles, findTaskUsers };
  }, [profile, store, roles, permissions, principals, loadAllRoles, loadAllPermissions, loadAllPrincipals]);


  return (<AccessMgmtContext.Provider value={contextValue}>
    {props.children}
  </AccessMgmtContext.Provider>);
}

function toRecord<V extends {id: string}>(iterable: V[]) {
  return [...iterable].reduce((obj, value) => {
    obj[value.id] = value
    return obj
  }, {} as {[k: string]: V})
}