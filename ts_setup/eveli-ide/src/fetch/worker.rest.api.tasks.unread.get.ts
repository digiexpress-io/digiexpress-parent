import { createFileFetch } from '@dxs-ts/eveli-fetch';

export const Hook = createFileFetch('worker/rest/api/tasks/unread.GET')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;


  return {
    loadNewTasks: async (): Promise<string[]> => {
      return params.fetch(url({path}))
        .then(response => response.json());
    }
  }
}