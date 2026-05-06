import { createFileFetch } from '@dxs-ts/envir-fetch';
import { Fs } from '../../fs-api';

export const Hook = createFileFetch('worker/rest/api/assets/fs/dirents/$id/bodies/$bodyType/transient-changes.POST')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;

  return {
    applyTransientChanges: (change: Fs.WrenchAstBodyChange): Promise<Fs.WorldFsBody> => {
      const payload = JSON.stringify(change);
      return params
        .fetch(url({ id: change.id, bodyType: change.bodyType }), {
          method,
          body: payload,
          headers: { 'Content-Type': 'application/json' },
        })
        .then(resp => resp.json())
        .then((data: any) => data as Fs.WorldFsBody);
    }
  }
}
