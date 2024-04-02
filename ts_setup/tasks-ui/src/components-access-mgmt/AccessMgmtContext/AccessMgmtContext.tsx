import React from 'react';

import Context from 'context';

import { ImmutablePermissionStore, Permission, Principal, Role } from 'descriptor-access-mgmt';

export interface AccessMgmtContextType {
  roles: Role[];
  permissions: Permission[];
  principals: Principal[];
  loading: boolean; // is permissions loading
  reload(): Promise<void>;
  getPermissionById(permissionId: string): Permission;
}



export function usePermissions() {
  const result = React.useContext(AccessMgmtContext);
  return result;
}

export const AccessMgmtContext = React.createContext<AccessMgmtContextType>({} as any);

export const AccessMgmtContextProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const backend = Context.useBackend();
  const [loading, setLoading] = React.useState(true);
  const [roles, setRoles] = React.useState<Role[]>([]);
  const [permissions, setPermissions] = React.useState<Permission[]>([]);
  const [principals, setPrincipals] = React.useState<Principal[]>([]); //TODO implement
  const [store] = React.useState(new ImmutablePermissionStore(backend.store));

  async function loadAllRoles(): Promise<void> {
    return store.findAllRoles().then(allRoles => {
      setLoading(false);
      if (allRoles.length) {
        setRoles(allRoles);
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
  }, []);


  const contextValue: AccessMgmtContextType = React.useMemo(() => {
    async function reload(): Promise<void> {
      setLoading(true);
      return Promise.all([loadAllRoles, loadAllPermissions]).then((values) => {
        console.log('loaded roles and permissions');
      });
    }
    function getPermissionById(permissionId: string) {
      const result = roles.find((role) => role.id === permissionId);
      if (!result) {
        throw new Error("Permission not found by id: " + permissionId);
      }
      return result;
    }

    return { loading, reload, roles, permissions, principals, getPermissionById };
  }, [loading, store, roles, permissions, principals]);


  return (<AccessMgmtContext.Provider value={contextValue}>
    {children}
  </AccessMgmtContext.Provider>);
}