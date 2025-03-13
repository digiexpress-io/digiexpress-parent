import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { PublicationInit } from '../frontdesk/types/Publication';
import { useIntl } from 'react-intl';
import { useSnackbar } from 'notistack';


export const Hook = createFileFetch('worker/rest/api/assets/publications.POST')({
  hook
}) 

const handleErrors = (response:Response) => {
  if (!response.ok) {
      throw Error(response.statusText);
  }
  return response;
}


function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();

  return {
    savePublication: async (init: PublicationInit, onSucess: () => void): Promise<void> => {
      return params.fetch(url({}), {
        method,
        headers: { 'Accept': 'application/json' },
        body: JSON.stringify(init)
      })
      .then(response => handleErrors(response))
      .then((response: any) => {
        onSucess();
      })
      .catch(error => {
        enqueueSnackbar(intl.formatMessage({ id: 'publications.tagCreationFailed' }, { cause: (error.message || 'N/A') }), { variant: 'error' });
      });
    }
  }
}