
import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { ContractApi } from '../contract-types';
import { useContractBackend } from '../contract-backend-provider';


export interface ContractContextType {
  contractContainer: ContractApi.ContractContainer
}

export const ContractContext = React.createContext<ContractContextType | undefined>(undefined);

export interface ContractProviderProps {
  contractId: ContractApi.ContractId;
  children: React.ReactNode
}

export const CONTRACT_QUERY_KEY = 'get-one-contract';
export const ContractProvider: React.FC<ContractProviderProps> = (props) => {
  const { contractId } = props;
  const backend = useContractBackend();
  const { data: contractContainer, error, refetch, isPending } = useQuery({
    queryKey: [CONTRACT_QUERY_KEY],
    queryFn: () => backend.persistence.getOneContract(contractId)
  });


  const contextValue: ContractContextType | undefined = React.useMemo(() => {
    if(isPending || !contractContainer) {
      return undefined
    }
    return { contractContainer };
  }, [contractId, backend, contractContainer, isPending]);

  if(contextValue) {
    return (<ContractContext.Provider value={contextValue}>{props.children}</ContractContext.Provider>);
  }
  return (<></>);
}

export function useContract() {
  const result = React.useContext(ContractContext);
  if(!result) {
    throw new Error('ContractContext is not created!');
  }
  return result;
}
