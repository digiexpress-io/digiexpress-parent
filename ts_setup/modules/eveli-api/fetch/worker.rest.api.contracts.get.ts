import { createFileFetch } from '@dxs-ts/envir-fetch';
import { ContractApi } from '@dxs-ts/contract-api';
import { useIntl } from 'react-intl';


export const Hook = createFileFetch('worker/rest/api/contracts.GET')({
  hook
})

function hook(_props: {}) {
  const params = Hook.useParams();
  const { url } = params;
  const intl = useIntl();
  
  return {
    findAllContracts: async (): Promise<ContractApi.ContractSummary[]> => {
      return params.fetch(`${url({}) }/all`)
        .then(response => response.json())
        .then((data: ContractApi.ContractContainer[]) => {
          return data.map((container) => {

            const [policyholder] = container.parties
                .filter(({ partyType }) => partyType === 'POLICYHOLDER')
                .sort(({ partyTermStartDate: a }, { partyTermStartDate: b}) => new Date(b).getTime() - new Date(a).getTime());

            const contract = container.contract;

            const result: ContractApi.ContractSummary = {
              container,

              policyholder,
              contractId: contract.id,
              contractNumber: contract.contractNumber,
              contractIssueDate: new Date(contract.contractIssueDate),
              contractStartDate: new Date(contract.contractStartDate),
              contractStatus: contract.contractStatus,
              contractType: contract.contractType,
              createdAt: new Date(contract.transitives?.createdAt!),
              updatedAt: new Date(contract.transitives?.updatedTreeAt!)
            };

            console.log(result);
            return {
              ...result,
              contractStatusIntl: intl.formatMessage({ id: `contract.status.${result.contractStatus?.toLowerCase()}`, defaultMessage: result.contractStatus })
            };
          });
        })
    },
    getOneContract: async (contractId: string): Promise<ContractApi.ContractContainer> => {
      return params.fetch(`${url({})}/${contractId}`)
        .then(response => response.json())
    }
  }
}