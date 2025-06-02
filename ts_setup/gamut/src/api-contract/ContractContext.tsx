import React from 'react'

import { ContractApi } from './contract-types';
import { usePopulateContext } from './usePopulateContext';


export const ContractContext = React.createContext<ContractApi.ContractContextType>({} as any);


export const ContractProvider: React.FC<{
  children: React.ReactNode;
  options: { staleTime: number, queryKey: string };
  appendContractAttachment: ContractApi.AppendContractAttachmentFetchPOST;
  getContracts: ContractApi.GetContractFetchGET;
  getContractAttachment: ContractApi.ContractAttachmentFetchGET;
}> = (props) => {
  const data = usePopulateContext(props);

  const [sortOrder, setSortOrder] = React.useState<ContractApi.ContractSortOrder>('DESC');
  const sortedByDate = data.contracts
    .filter((c) => !!c.updated)
    .sort((a, b) => {
      const dateA = a.updated ? a.updated.toMillis() : 0;
      const dateB = b.updated ? b.updated.toMillis() : 0;
      return sortOrder === 'ASC' ? dateA - dateB : dateB - dateA;
    });

  function toggleContractSortOrder() {
    setSortOrder((prev) => (prev === 'ASC' ? 'DESC' : 'ASC'));
  };

  return React.useMemo(() => {
    const awaitingDecision = data.contracts.filter((c) => c.status === 'OPEN' || c.status === 'NEW' || c.status === 'TRANSFERRED' || c.status === 'WAITING');
    const decided = data.contracts.filter((c) => c.status === 'COMPLETED' || c.status === 'REJECTED' || c.status === 'DELEGATED');

    const contextValue: ContractApi.ContractContextType = {
      contracts: sortedByDate,
      isPending: data.isPending,
      getContract: (id) => {
        return data.contracts.find((contract) => contract.id === id);
      },
      toggleContractSortOrder,
      sortOrder,
      refresh: data.refresh,
      appendContractAttachment: data.appendContractAttachment,
      getContractAttachment: data.getContractAttachment,
      contractStats: Object.freeze({ awaitingDecision: awaitingDecision.length, decided: decided.length })
    };
    return (
      <ContractContext.Provider value={contextValue}>
        {props.children}
      </ContractContext.Provider>);

  }, [data, props, sortOrder]);
}


export function useContracts(): ContractApi.ContractContextType {
  const result: ContractApi.ContractContextType = React.useContext(ContractContext);
  return result;
}

