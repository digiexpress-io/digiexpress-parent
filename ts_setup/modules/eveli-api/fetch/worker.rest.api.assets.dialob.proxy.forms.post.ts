import { createFileFetch } from '@dxs-ts/envir-fetch';

import { useSnackbar } from 'notistack';
import { useIntl } from 'react-intl';

import { DialobForm } from './worker.rest.api.assets.dialob.get';

export const Hook = createFileFetch('worker/rest/api/assets/dialob/proxy/forms.POST')({
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
    saveForm: async (form: Partial<DialobForm>) => {
      return params.fetch(url({}), {
        method,
        headers: { 'Accept': 'application/json' },
        body: JSON.stringify(form)
      })
      .then((response: Response)=>handleErrors(response))
      .then((response: Response) => response.json())
      .then ((json: any) => json)
      .catch((error: any) => {
        enqueueSnackbar(intl.formatMessage({id: 'dialobForm.saveFailed'}, {cause: (error.message || 'N/A')}), {variant: 'error'});
      });
    }
  }
}