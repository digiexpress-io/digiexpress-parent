import { createFileFetch } from '@dxs-ts/envir-fetch';
import { Fs } from '../../fs-api';

export const Hook = createFileFetch('worker/rest/api/assets/fs.GET')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;

  return {
    getDirents: (): Promise<Fs.DirentBase[]> => {
      return params
        .fetch(url({}) + '/dirents', { method })
        .then(resp => resp.json())
        .then((data: any) => data.dirents
        );

    }

  }
}