import { createFileFetch } from '@dxs-ts/envir-fetch';
import { Fs } from '../../fs-api';

export const Hook = createFileFetch('worker/rest/api/assets/fs/debugs.POST')({ hook })

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  return {
    debugDirent: (debug: { id: string; input?: string; inputCSV?: string }): Promise<Fs.DebugResponse> => {
      return params
        .fetch(url({}), { method, body: JSON.stringify(debug), headers: { 'Content-Type': 'application/json' } })
        .then(resp => resp.json())
        .then((data: any) => data as Fs.DebugResponse);
    }
  }
}
