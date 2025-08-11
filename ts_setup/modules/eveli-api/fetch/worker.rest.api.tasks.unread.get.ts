import { createFileFetch } from '@dxs-ts/envir-fetch';
import { useQuery } from '@tanstack/react-query'

export const Hook = createFileFetch('worker/rest/api/tasks/unread.GET')({
  hook
})

function hook(props: {}): { unreadTasks: string[] } {
  const params = Hook.useParams();
  const { path, url } = params;
  
  const { data, error, refetch, isPending } = useQuery({
    queryKey: ['tasks/unread'],
    queryFn: () => params.fetch(url({path})).then(resp => resp.json())
  });

  return {
    unreadTasks: data ?? []
  }
}
