import React from 'react';
import Context from 'context';

import { ImmutableAccessMgmtStore, Permission, Principal, Role } from 'descriptor-access-mgmt';

export interface AccessMgmtContextType {
  roles: Role[];
  permissions: Permission[];
  principals: Principal[];
  loading: boolean; // is permissions loading
  reload(): Promise<void>;
  getPermission(idOrNameOrExternalId: string): Permission;
  getPrincipal(idOrNameOrExternalId: string): Principal;
  getRole(idOrNameOrExternalId: string): Role;
}



export function useAccessMgmt() {
  const result = React.useContext(AccessMgmtContext);
  return result;
}

export const AccessMgmtContext = React.createContext<AccessMgmtContextType>({} as any);

export const AccessMgmtContextProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const backend = Context.useBackend();
  const [loading, setLoading] = React.useState(true);
  const [roles, setRoles] = React.useState<Role[]>([]);
  const [permissions, setPermissions] = React.useState<Permission[]>([]);
  const [principals, setPrincipals] = React.useState<Principal[]>([]);
  const [store] = React.useState(new ImmutableAccessMgmtStore(backend.store));

  async function loadAllRoles(): Promise<void> {
    return store.findAllRoles().then(allRoles => {
      setLoading(false);
      if (allRoles.length) {
        setRoles(allRoles);
      }
    })
      .catch(() => setLoading(false));
  }

  async function loadAllPrincipals(): Promise<void> {
    return store.findAllPrincipals().then(allPrincipals => {
      setLoading(false);
      if (allPrincipals.length) {
        setPrincipals(allPrincipals);
      }
    })
      .catch(() => setLoading(false));
  }

  async function loadAllPermissions(): Promise<void> {
    return store.findAllPermissions().then(allPermissions => {
      setLoading(false);
      if (allPermissions.length) {
        setPermissions(allPermissions);
      }
    })
      .catch(() => setLoading(false));
  }

  // perform init
  React.useEffect(() => {
    loadAllRoles();
    loadAllPermissions();
    loadAllPrincipals();
  }, []);


  const contextValue: AccessMgmtContextType = React.useMemo(() => {
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
        throw new Error("Principal not found by id/name/externalId" + idOrName);
      }
      return result;
    }
    return { loading, reload, roles, permissions, principals, getPermission, getRole, getPrincipal };
  }, [loading, store, roles, permissions, principals, loadAllRoles, loadAllPermissions, loadAllPrincipals]);


  return (<AccessMgmtContext.Provider value={contextValue}>
    {children}
  </AccessMgmtContext.Provider>);
}