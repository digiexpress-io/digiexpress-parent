import { createFileFetch } from '@dxs-ts/envir-fetch';
import { useSnackbar } from 'notistack';
import { useIntl } from 'react-intl';

export const Hook = createFileFetch('worker/rest/api/assets/dialob/fill/$sessionId.POST')({
  hook
}) 


function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();

  return {
    saveDialobSession: async (sessionId: string, actions: any[], rev: number): Promise<Response> => {
      return params.fetch(url({ sessionId }), {
        method,
        headers: {
          'Accept': 'application/json'
        },
        body: JSON.stringify({ rev, actions }),
      })
      .then(response => {
        if(!response.ok) {
          enqueueSnackbar(intl.formatMessage({ id: 'dialob.fetch.post.failed' }, { cause: (response.statusText || 'N/A') }), { variant: 'error' });  
        }
        return response;
      }) 
    }
  }
}


