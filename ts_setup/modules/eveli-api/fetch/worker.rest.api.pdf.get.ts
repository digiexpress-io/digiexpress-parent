import { createFileFetch } from '@dxs-ts/envir-fetch';


export const Hook = createFileFetch('worker/rest/api/pdf.GET')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;

  return {

    pdfTaskLinkCallback: async (questionnaireId: string, taskId: string) => {
      return `${url({})}?taskId=${taskId}&questionnaireId=${questionnaireId}`;
    }

  }
}