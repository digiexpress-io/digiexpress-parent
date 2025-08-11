import { createFileFetch } from '@dxs-ts/envir-fetch';


export const Hook = createFileFetch('worker/rest/api/iam/liveness.GET')({
  hook
})

function hook(props: {}): {
  livenessUrl: string,
  getLiveness: () => Promise<{ expiresIn: number } | undefined>
} {
  const params = Hook.useNativeParams();
  const { method, url } = params;
  const serviceUrl = url({ });

  return {
    livenessUrl: serviceUrl,

    getLiveness: async () => {
      const response = await window.fetch(serviceUrl, {
        method: method,
        headers: undefined,
        credentials: undefined,
      })
      if(!response.ok) {
        return undefined;
      }
      return await response.json();
    }
  }
}