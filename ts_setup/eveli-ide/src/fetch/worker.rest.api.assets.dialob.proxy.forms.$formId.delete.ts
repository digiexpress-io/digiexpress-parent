import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { Workflow } from '../frontdesk/types/Workflow';
import { useSnackbar } from 'notistack';
import { useIntl } from 'react-intl';

export const Hook = createFileFetch('worker/rest/api/assets/dialob/proxy/forms/$formId.DELETE')({
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
    deleteDialog: async (formId: string, onSuccess: () => void) => {      
      return params.fetch(url({ formId }), { method })
        .then((response:Response)=>handleErrors(response))
        .then((response:Response) => response.json())
        .then ((json:any) => {
          onSuccess();
          return json;
        })
        .catch((error:any) => {
          enqueueSnackbar(intl.formatMessage({id: 'dialobForm.deleteFailed'}, {cause: (error.message || 'N/A')}), {variant: 'error'});
        });
    }
  }
}