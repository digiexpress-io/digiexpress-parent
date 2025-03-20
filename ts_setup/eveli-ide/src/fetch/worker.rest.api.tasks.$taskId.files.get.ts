import { TaskApi } from '@/burger';
import { createFileFetch } from '@dxs-ts/eveli-fetch';


export const Hook = createFileFetch('worker/rest/api/tasks/$taskId/files.GET')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;

  return {

    loadAttachments: async (taskId: string): Promise<TaskApi.Attachment[]> => {
      return params.fetch(url({ taskId }) + '/', { method })
        .then(response => response.json());
    },

  }
}