
import { TaskApi } from '@/api-task';
import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { useSnackbar } from 'notistack';
import { useIntl } from 'react-intl';


export const Hook = createFileFetch('worker/rest/api/tasks/$taskId/files.POST')({
  hook
})

function hook(props: {}) {

  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;
  const { enqueueSnackbar } = useSnackbar();
  const intl = useIntl();


  function handleErrors(response: Response) {
    if (!response.ok) {
      throw Error(response.statusText);
    }
    return response;
  }

  async function uploadFile(file: File, uploadResponse: TaskApi.AttachmentUploadResponse) {
    return params
    .fetch(uploadResponse.putRequestUrl, {method:'PUT', body: file, headers: {'Content-Type': file.type || 'application/octet-stream'}})
    .then(handleErrors)
    .then(response => {
      enqueueSnackbar(intl.formatMessage({id: 'attachment.uploadOk'}, {fileName: file.name}), {variant: 'success'});
      return response;
    })
  }


  return {
    addAttachment: async(taskId: string, file: File) : Promise<Response|void> => {
      const fileName = file.name;
      return params
        .fetch(url({ taskId }) + `/?filename=${fileName}`, { method, headers: {'Content-Type': file.type || 'application/octet-stream'}})
        .then(handleErrors)
        .then(response => response.json())
        .then((uploadResponse: TaskApi.AttachmentUploadResponse) => uploadFile(file, uploadResponse))
        .catch(_error => {
          enqueueSnackbar(intl.formatMessage({id: 'attachment.uploadFailed'}, { fileName }), { variant: 'error' });
        });
    }
  }
}