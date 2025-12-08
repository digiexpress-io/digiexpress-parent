
import React from 'react';
import { LedgerApi } from '../ledger-types';

export interface LedgerBackendContextType {
  deps: any[];
  currentUser: { name: string; email: string; };
  roles: string[];
  permissions: {};
  features: {};
  navigate: {
    findAllLedgers: () => void;
    openOneLedger: (contractIdOrRef: string) => void;
  };
  persistence: {
    findAllLedgers: () => Promise<LedgerApi.LedgerSummary[]>;
    getOneLedger: (contractId: string) => Promise<LedgerApi.LedgerContainer>;
  };
  slots: {
    DateTimeFormatter: React.ElementType<{ value: string | Date | undefined, variant?: 'text' }>;
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



export const LedgerBackendContext = React.createContext<LedgerBackendContextType>({} as any);

export interface LedgerBackendProviderProps {
  deps: any[];
  children: React.ReactNode;
  roles: LedgerBackendContextType['roles'];
  permissions: LedgerBackendContextType['permissions'];
  currentUser: LedgerBackendContextType['currentUser'];
  features: LedgerBackendContextType['features'];
  navigate: LedgerBackendContextType['navigate'];
  slots: LedgerBackendContextType['slots'];
  persistence: LedgerBackendContextType['persistence'];
}

export const LedgerBackendProvider: React.FC<LedgerBackendProviderProps> = (props) => {
  const { navigate, persistence, slots, deps, permissions, currentUser, features, roles } = props;

  const contextValue: LedgerBackendContextType = React.useMemo(() => {
    return { navigate, persistence, slots, deps, permissions, currentUser, features, roles };
  }, [deps]);

  return (<LedgerBackendContext.Provider value={contextValue}>{props.children}</LedgerBackendContext.Provider>);
}

export function useLedgerBackend() {
  const result: LedgerBackendContextType = React.useContext(LedgerBackendContext);
  return result;
}
