import { TaskApi } from '@/api-task';
import { createFileFetch } from '@dxs-ts/eveli-fetch';


export const Hook = createFileFetch('worker/rest/api/pdf.GET')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;

  return {

    pdfTaskLinkCallback: (questionnaireId: string, taskId: string) => {
      window.open(`${url({})}?taskId=${taskId}&questionnaireId=${questionnaireId}`);
    }

  }
}