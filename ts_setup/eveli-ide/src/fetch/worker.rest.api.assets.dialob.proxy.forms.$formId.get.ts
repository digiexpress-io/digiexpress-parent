import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { useSnackbar } from 'notistack';
import { useIntl } from 'react-intl';

export const Hook = createFileFetch('worker/rest/api/assets/dialob/proxy/forms/$formId.GET')({
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
  const { enqueueSnackbar } = useSnackbar();
  const intl = useIntl();

  return {
    getForm: async (formId: string) => {
      return params.fetch(url({formId}),{
        method,
        headers: {
          'Accept': 'application/json'
        }
      })
      .then((response:Response) => handleErrors(response))
      .then((response:Response) => response.json())
      .catch((error:any) => {
        enqueueSnackbar(intl.formatMessage({id: 'dialobForm.downloadFailed'}, {cause: (error.message || 'N/A')}), {variant: 'error'});
      });
    }
  }
}