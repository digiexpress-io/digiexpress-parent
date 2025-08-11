import { createFileFetch } from '@dxs-ts/envir-fetch';
import { useSnackbar } from 'notistack';
import { useIntl } from 'react-intl';

export const Hook = createFileFetch('worker/rest/api/assets/dialob/fill/$sessionId.GET')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();

  return {
    getDialobSession: async (sessionId: string): Promise<Response> => {
      return params.fetch(url({ sessionId }), {
        method,
        headers: {
          'Accept': 'application/json'
        },
      })
      .then(response => {
        if(!response.ok) {
          enqueueSnackbar(intl.formatMessage({ id: 'dialob.fetch.get.failed' }, { cause: (response.statusText || 'N/A') }), { variant: 'error' });  
        }
        return response;
      }) 
    }
  }
}

