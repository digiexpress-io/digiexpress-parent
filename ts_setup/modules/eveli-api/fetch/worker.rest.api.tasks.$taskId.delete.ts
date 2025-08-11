import { createFileFetch } from '@dxs-ts/envir-fetch';
import { useSnackbar } from 'notistack';
import { useIntl } from 'react-intl';


export const Hook = createFileFetch('worker/rest/api/tasks/$taskId.DELETE')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;
  const { enqueueSnackbar } = useSnackbar();
  const intl = useIntl();
  
  return {
    deleteTask: async (taskId: string): Promise<{}> => {
      return params
        .fetch(url({ taskId }), { method })
        .then(response => {
          if (!response.ok) {
            let message = 'error.dataAccess';
            if (response.status === 403) {
              message = 'error.unauthorizedAccess';
            }
            enqueueSnackbar(intl.formatMessage({id: message}), { variant: 'error' });
          }
          return response;
        });
    }
  }
}