import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { TaskLink } from '../frontdesk/types/task/TaskLink';


export const Hook = createFileFetch('worker/rest/api/pdf.GET')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;

  return {

    pdfTaskLinkCallback: (link: TaskLink, taskId: string) => {
      window.open(`${url({})}?taskId=${taskId}&questionnaireId=${link.linkAddress}`);
    }

  }
}