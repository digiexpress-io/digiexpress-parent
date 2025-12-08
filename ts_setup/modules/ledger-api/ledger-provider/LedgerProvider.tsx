
import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { LedgerApi } from '../ledger-types';
import { useLedgerBackend } from '../ledger-backend-provider';


export interface LedgerContextType {
  ledgerContainer: LedgerApi.LedgerContainer
}

export const LedgerContext = React.createContext<LedgerContextType | undefined>(undefined);

export interface LedgerProviderProps {
  ledgerId: LedgerApi.LedgerId;
  children: React.ReactNode
}

export const LEDGER_QUERY_KEY = 'get-one-ledger';
export const LedgerProvider: React.FC<LedgerProviderProps> = (props) => {
  const { ledgerId } = props;
  const backend = useLedgerBackend();
  const { data: ledgerContainer, error, refetch, isPending } = useQuery({
    queryKey: [LEDGER_QUERY_KEY],
    queryFn: () => backend.persistence.getOneLedger(ledgerId)
  });


  const contextValue: LedgerContextType | undefined = React.useMemo(() => {
    if(isPending || !ledgerContainer) {
      return undefined
    }
    return { ledgerContainer };
  }, [ledgerId, backend, ledgerContainer, isPending]);

  if(contextValue) {
    return (<LedgerContext.Provider value={contextValue}>{props.children}</LedgerContext.Provider>);
  }
  return (<></>);
}

export function useLedger() {
  const result = React.useContext(LedgerContext);
  if(!result) {
    throw new Error('LedgerContext is not created!');
  }
  return result;
}
