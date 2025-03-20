import { TaskApi } from '@/burger';
import { createFileFetch } from '@dxs-ts/eveli-fetch';


export const Hook = createFileFetch('worker/rest/api/pdf.GET')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;

  return {

    pdfTaskLinkCallback: (link: TaskApi.TaskLink, taskId: string) => {
      window.open(`${url({})}?taskId=${taskId}&questionnaireId=${link.linkAddress}`);
    }

  }
}