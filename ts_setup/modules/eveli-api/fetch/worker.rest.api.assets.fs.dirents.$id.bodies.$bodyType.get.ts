import { createFileFetch } from '@dxs-ts/envir-fetch';
import { Fs } from '../../fs-api';

export const Hook = createFileFetch('worker/rest/api/assets/fs/dirents/$id/bodies/$bodyType.GET')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;

  return {
    getDirentBody: (id: string, bodyType: Fs.BodyType): Promise<Fs.WorldFsBody> => {
      return params
        .fetch(url({ id, bodyType }))
        .then(resp => resp.json())
        .then((data: any) => data as Fs.WorldFsBody);
    }
  }
}