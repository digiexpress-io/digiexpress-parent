
import { PermissionId } from 'descriptor-access-mgmt';
import React from 'react';


interface PermissionsOverviewContextType {
  permissionId: PermissionId | undefined,
  setActive(id: string): void;
}

const PermissionsOverviewContext = React.createContext<PermissionsOverviewContextType>({} as any);

export const PermissionsOverviewContextProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [permissionId, setActive] = React.useState<PermissionId>();

  const contextValue: PermissionsOverviewContextType = React.useMemo(() => {

    return {
      permissionId,
      setActive
    }
  }, [permissionId]);

  return (
    <PermissionsOverviewContext.Provider value={contextValue}>{children}</PermissionsOverviewContext.Provider>
  );
};

export function useActivePermission() {
  const result: PermissionsOverviewContextType = React.useContext(PermissionsOverviewContext);
  return result;
};
