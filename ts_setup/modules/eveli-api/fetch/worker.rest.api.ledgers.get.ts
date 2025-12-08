import { createFileFetch } from '@dxs-ts/envir-fetch';
import { LedgerApi } from '@dxs-ts/ledger-api';
import { useIntl } from 'react-intl';


export const Hook = createFileFetch('worker/rest/api/ledgers.GET')({
  hook
})

function hook(_props: {}) {
  const params = Hook.useParams();
  const { url } = params;
  const intl = useIntl();
  
  return {
    findAllLedgers: async (): Promise<LedgerApi.LedgerSummary[]> => {
      return params.fetch(`${url({}) }/all`)
        .then(response => response.json())
        .then((data: LedgerApi.LedgerContainer[]) => {
          return data.map((container) => {
            const ledger = container.ledger;
            const result: LedgerApi.LedgerSummary = {
              ledgerId: ledger.id,
              contractNumber: ledger.name,
              createdAt: new Date(ledger.transitives?.createdAt!),
              updatedAt: new Date(ledger.transitives?.updatedTreeAt!)
            };
            return { ...result };
          });
        })
    },
    getOneLedger: async (ledgerId: string): Promise<LedgerApi.LedgerContainer> => {
      return params.fetch(`${url({})}/${ledgerId}`)
        .then(response => response.json())
    }
  }
}