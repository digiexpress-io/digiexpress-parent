import { createFileFetch } from '@dxs-ts/envir-fetch';
import { ContractApi } from '@dxs-ts/contract-api';


export const Hook = createFileFetch('worker/rest/api/contracts.GET')({
  hook
})

function hook(_props: {}) {
  const params = Hook.useParams();
  const { url } = params;
  
  return {
    findAllContracts: async (): Promise<ContractApi.Contract[]> => {
      return params.fetch(`${url({}) }/all`)
        .then(response => response.json())
    },
    getOneContract: async (contractId: string): Promise<ContractApi.Contract> => {
      return params.fetch(`${url({}) }/${contractId}`)
        .then(response => response.json())
    }
  }
}