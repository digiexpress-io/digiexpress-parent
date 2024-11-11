import React from 'react';


import { useQuery } from '@tanstack/react-query'
import { ContractApi } from './contract-types';
import { LegacyProcessApi } from '../api-legacy-processes';


import { mapToContractData } from './mappers'
import { useSite } from '../api-site';



export interface UsePropulateProps {
  options: { staleTime: number, queryKey: string };
  appendContractAttachment: ContractApi.AppendContractAttachmentFetchPOST;
  getContracts: ContractApi.GetContractFetchGET;
}

export interface PopulateContractContext {
  contracts: readonly ContractApi.Contract[];
  isPending: boolean;
  appendContractAttachment: (contractId: ContractApi.ContractId, files: FileList) => Promise<ContractApi.Contract>;
  refresh(): Promise<void>;
}

export function usePopulateContext(props: UsePropulateProps): PopulateContractContext {
  const { site } = useSite();
  const [isInitialLoadDone, setInitialLoadDone] = React.useState(false);
  const { getContracts, options } = props;
  const { staleTime, queryKey } = options;

  // tanstack query config
  const { data: processes, error, refetch, isPending } = useQuery({
    staleTime,
    queryKey: [queryKey],
    queryFn: () => getContracts()
      .then(data => data.json())
      .then((data: LegacyProcessApi.Process[]) => data),
  });

  const contractData = mapToContractData(processes ?? [], site);

  // Create new contract and reload after that
  const appendContractAttachment: (contractId: ContractApi.ContractId, files: FileList) => Promise<ContractApi.Contract> = React.useCallback(async (contractId, files) => {
    await props.appendContractAttachment(contractId, files);
    return refetch()
      .then((refetched) => mapToContractData(refetched.data ?? [], site)
        .contracts
        .find(c => c.id === contractId)!);

  }, [refetch, props.appendContractAttachment, site]);

  // Reload all data
  const refresh: () => Promise<void> = React.useCallback(async () => {
    return refetch().then(() => { });
  }, [refetch]);


  // track initial loading
  React.useEffect(() => {
    if (isInitialLoadDone) {
      return;
    }
    if (contractData) {
      setInitialLoadDone(true);
    }
  }, [isInitialLoadDone, contractData]);


  const isContextLoaded = (isInitialLoadDone || !isPending);

  // cache the end result
  return React.useMemo(() => {
    return { contracts: contractData?.contracts ?? [], isPending: !isContextLoaded, appendContractAttachment, refresh };
  }, [contractData?.hash, isContextLoaded, appendContractAttachment, refresh]);
}
