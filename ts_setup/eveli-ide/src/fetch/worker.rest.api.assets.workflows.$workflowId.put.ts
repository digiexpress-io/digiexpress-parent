import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { Workflow } from '../frontdesk/types/Workflow';
import { useSnackbar } from 'notistack';
import { useIntl } from 'react-intl';

export const Hook = createFileFetch('worker/rest/api/assets/workflows/$workflowId.PUT')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  const { enqueueSnackbar } = useSnackbar();
  const intl = useIntl();

  return {
    update: async(workflowCommand: Workflow, onOk: () => void) => {

      return params.fetch(url({workflowId: workflowCommand.id}), {
        method,
        headers: {
          'Accept': 'application/json'
        },
        body: JSON.stringify({ ...workflowCommand.body, ...(workflowCommand.id ? { id: workflowCommand.id } : {}) })
      })
      .then((response: any) => {
        if (response.ok) {
          onOk();
        } else {
          enqueueSnackbar(intl.formatMessage({ id: 'error.workflowCreation' }), { variant: 'error' });
        }
      })
    }
  }
}