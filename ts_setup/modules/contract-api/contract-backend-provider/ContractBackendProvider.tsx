
import React from 'react';
import { ContractApi } from '../contract-types';

export interface ContractBackendContextType {
  deps: any[];
  currentUser: { name: string; email: string; };
  roles: string[];
  permissions: {};
  features: {};
  navigate: {
    findAllContracts: () => void;
    openOneContract: (contractIdOrRef: string) => void;
  };
  persistence: {
    findAllContracts: () => Promise<ContractApi.ContractSummary[]>;
    getOneContract: (contractId: string) => Promise<ContractApi.ContractContainer>;
  };
  slots: {
    DateTimeFormatter: React.ElementType<{
      value: string | Date | undefined;
      withSeconds?: boolean;
      withColumns?: boolean;
    }>;
    DateTimePicker: React.ElementType<{ 
      label?: string | React.ReactNode,
      readonly?: boolean,
      fullWidth?: boolean,
      value: string | Date | undefined | null;
      onChange?: (newValue: Date | null) => void;
      onKeyDown?: (event: React.KeyboardEvent<HTMLInputElement>) => void;
    }>
  }
}



export const ContractBackendContext = React.createContext<ContractBackendContextType>({} as any);

export interface ContractBackendProviderProps {
  deps: any[];
  children: React.ReactNode;
  roles: ContractBackendContextType['roles'];
  permissions: ContractBackendContextType['permissions'];
  currentUser: ContractBackendContextType['currentUser'];
  features: ContractBackendContextType['features'];
  navigate: ContractBackendContextType['navigate'];
  slots: ContractBackendContextType['slots'];
  persistence: ContractBackendContextType['persistence'];
}

export const ContractBackendProvider: React.FC<ContractBackendProviderProps> = (props) => {
  const { navigate, persistence, slots, deps, permissions, currentUser, features, roles } = props;

  const contextValue: ContractBackendContextType = React.useMemo(() => {
    return { navigate, persistence, slots, deps, permissions, currentUser, features, roles };
  }, [deps]);

  return (<ContractBackendContext.Provider value={contextValue}>{props.children}</ContractBackendContext.Provider>);
}

export function useContractBackend() {
  const result: ContractBackendContextType = React.useContext(ContractBackendContext);
  return result;
}
