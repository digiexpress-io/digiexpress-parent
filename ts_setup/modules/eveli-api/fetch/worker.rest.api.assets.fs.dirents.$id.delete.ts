import { createFileFetch } from '@dxs-ts/envir-fetch';
import { Fs } from '@dxs-ts/fs-api';

export const Hook = createFileFetch('worker/rest/api/assets/fs/dirents/$id.DELETE')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;

  return {
    deleteAny: async (id: string, bodyType: Fs.BodyType): Promise<void> => {
      await params.fetch(`${url({ id })}?bodyType=${bodyType}`, { method });
    }
  }
}
