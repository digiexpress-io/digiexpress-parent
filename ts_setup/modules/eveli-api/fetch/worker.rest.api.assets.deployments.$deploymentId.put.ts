import { createFileFetch } from '@dxs-ts/envir-fetch';
import { useIntl } from 'react-intl';
import { useSnackbar } from 'notistack';


export const Hook = createFileFetch('worker/rest/api/assets/deployments/$deploymentId.PUT')({
  hook
}) 



function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();

  return {
    saveDeployment: async (init: { status: string, id: string }, onSucess: () => void): Promise<void> => {
      return params.fetch(url({ deploymentId: init.id }), {
        method,
        headers: {
          'Accept': 'application/json'
        },
        body: JSON.stringify(init)
      })
      .then((response: any) => {
        onSucess()
      })
      .catch(error => {
        enqueueSnackbar(intl.formatMessage({ id: 'publications.statusChangeFailed' }, { cause: (error.message || 'N/A') }), { variant: 'error' });
      });
    }
  }
}