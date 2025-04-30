import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { enqueueSnackbar } from 'notistack';
import { useIntl } from 'react-intl';


export const Hook = createFileFetch('worker/rest/api/tasks/$taskId/files/$filename.DELETE')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;
  const intl = useIntl();
  return {
    deleteAttachment: (taskId: string, filename: string): Promise<Response|void> => {
      return params.fetch(url({ filename, taskId }), {method})
      .catch(_error => {
        enqueueSnackbar(intl.formatMessage({id: 'attachment.deleteFailed'}, { filename }), { variant: 'error' });
      });
    }
  
  }
}