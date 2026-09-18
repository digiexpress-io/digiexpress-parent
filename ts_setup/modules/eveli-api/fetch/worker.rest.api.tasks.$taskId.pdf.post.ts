import { createFileFetch } from '@dxs-ts/envir-fetch';
import { useSnackbar } from 'notistack';
import { useIntl } from 'react-intl';
import { TaskApi } from '@dxs-ts/task-api';


export const Hook = createFileFetch('worker/rest/api/tasks/$taskId/pdf.POST')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { method, url } = params;

  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();

  return {
    pdfTaskCallback: async (request: TaskApi.TaskPdfRequest): Promise<Blob> => {
      const { taskId, questionnaireId, fields } = request;
      return params.fetch(url({ taskId }), {
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/pdf' // Explicitly request PDF
        },
        method,
        body: JSON.stringify({ questionnaireId, fields })
      })
      .then(response => {
        if(response.status === 409) {
          enqueueSnackbar(intl.formatMessage({ id: 'task.pdf.notCompleted' }), { variant: 'warning' });
          throw Error(response.statusText);
        }
        if(!response.ok) {
          enqueueSnackbar(intl.formatMessage({ id: 'task.pdf.failed' }, { cause: (response.statusText || 'N/A') }), { variant: 'error' });
          throw Error(response.statusText);
        }
        return response.blob();
      })
    }
  }
}
