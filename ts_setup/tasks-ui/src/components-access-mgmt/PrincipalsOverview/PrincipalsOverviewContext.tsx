import { PrincipalId } from 'descriptor-access-mgmt';
import React from 'react';


interface PrincipalsOverviewContextType {
  principalId: string | undefined;
  setActivePrincipal(id: string): void;
}

const PrincipalsOverviewContext = React.createContext<PrincipalsOverviewContextType>({} as any);

export const PrincipalsOverviewProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [principalId, setActivePrincipal] = React.useState<PrincipalId>();

  const contextValue: PrincipalsOverviewContextType = React.useMemo(() => {
    return {
      principalId,
      setActivePrincipal,
    }
  }, [principalId]);

  return (<PrincipalsOverviewContext.Provider value={contextValue}>{children}</PrincipalsOverviewContext.Provider>)
}

export function useActivePrincipal() {
  const result: PrincipalsOverviewContextType = React.useContext(PrincipalsOverviewContext);
  return result;
}

