import { createFileFetch } from '@dxs-ts/envir-fetch';
import { TaskApi } from '@dxs-ts/task-api';


export const Hook = createFileFetch('worker/rest/api/pdf.POST')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { method, url } = params;

  return {
    pdfTaskCallback: async (props: TaskApi.TaskPdfRequest): Promise<Blob> => {
      return params.fetch(url({}), {
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/pdf' // Explicitly request PDF
        },
        method,
        body: JSON.stringify(props)
      }).then(resp => resp.blob())
    }
  }
}