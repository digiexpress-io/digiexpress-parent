import React from 'react'

import { ContractApi } from './contract-types';
import { usePopulateContext } from './usePopulateContext';



export const ContractContext = React.createContext<ContractApi.ContractContextType>({} as any);


export const ContractProvider: React.FC<{
  children: React.ReactNode;
  options: { staleTime: number, queryKey: string };
  appendContractAttachment: ContractApi.AppendContractAttachmentFetchPOST;
  getContracts: ContractApi.GetContractFetchGET;
}> = (props) => {
  const data = usePopulateContext(props);

  return React.useMemo(() => {

    const awaitingDecision = data.contracts.filter((c) => c.status === 'OPEN' || c.status === 'NEW' );
    const decided = data.contracts.filter((c) => c.status === 'COMPLETED' || c.status === 'REJECTED');

    const contextValue: ContractApi.ContractContextType = {
      contracts: data.contracts,
      isPending: data.isPending,
      getContract: (id) => data.contracts.find((contract) => contract.id === id),
      refresh: data.refresh,
      appendContractAttachment: data.appendContractAttachment,
      contractStats: Object.freeze({ awaitingDecision: awaitingDecision.length, decided: decided.length })
    };

    return (<ContractContext.Provider value={contextValue}>{props.children}</ContractContext.Provider>);
  }, [data, props]);
}


export function useContracts(): ContractApi.ContractContextType {
  const result: ContractApi.ContractContextType = React.useContext(ContractContext);
  return result;
}

