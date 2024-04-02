import React from 'react';

import Context from 'context';

import { ImmutablePermissionStore, Permission, Principal, Role } from 'descriptor-access-mgmt';
import { PermissionsContextType } from './permissions-context-types';



export function usePermissions() {
  const { roles, permissions, principals } = React.useContext(AccessMgmtContext);

  return {
    roles,
    permissions,
    principals
  };
}


export const AccessMgmtContext = React.createContext<PermissionsContextType>({} as any);

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


  const contextValue: PermissionsContextType = React.useMemo(() => {
    async function reload(): Promise<void> {
      setLoading(true);
      return Promise.all([loadAllRoles, loadAllPermissions]).then((values) => {
        console.log('loaded roles and permissions');
      });
    }
    return { loading, reload, roles, permissions, principals };
  }, [loading, store, roles, permissions, principals]);


  return (<AccessMgmtContext.Provider value={contextValue}>
    {children}
  </AccessMgmtContext.Provider>);
}