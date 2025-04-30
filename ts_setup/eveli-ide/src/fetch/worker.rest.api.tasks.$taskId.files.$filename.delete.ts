import { createFileFetch } from '@dxs-ts/eveli-fetch';


export const Hook = createFileFetch('worker/rest/api/tasks/$taskId/files/$filename.DELETE')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;

  return {
    deleteAttachment: (taskId: string, filename: string): string => {
      return url({ filename, taskId })
    }
  
  }
}