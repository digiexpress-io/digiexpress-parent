import React from 'react';
import { PermissionId } from 'descriptor-access-mgmt';


interface PermissionsOverviewContextType {
  permissionId: PermissionId | undefined,
  setActivePermission(id: string): void;
}

const PermissionsOverviewContext = React.createContext<PermissionsOverviewContextType>({} as any);

export const PermissionsOverviewProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [permissionId, setActivePermission] = React.useState<PermissionId>();

  const contextValue: PermissionsOverviewContextType = React.useMemo(() => {

    return {
      permissionId,
      setActivePermission
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
