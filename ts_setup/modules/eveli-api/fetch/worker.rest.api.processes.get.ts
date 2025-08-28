import { createFileFetch } from '@dxs-ts/envir-fetch';
import { ProcExecutionApi } from '../api-proc-execution';



export const Hook = createFileFetch('worker/rest/api/processes.GET')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;

  return {


    findLast6Months: async (): Promise<ProcExecutionApi.ProcessExecution[]> => {
      return params.fetch(url({}) + `/last-6-months`, {
        headers: {
          'Accept': 'application/json'
        },
      })
      .then(response => response.json())
    }
  
  }
}