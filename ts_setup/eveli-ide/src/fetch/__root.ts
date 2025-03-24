import { useIamForcedLogin } from '@/api-iam';
import { createRootFileFetch } from '@dxs-ts/eveli-fetch';


export const Hook = createRootFileFetch('', uberFetchWithAuthAndErrorHandling);


function uberFetchWithAuthAndErrorHandling()  {
  const { loginOn401 } = useIamForcedLogin();




  return async (input: RequestInfo | URL, init?: RequestInit) => {
    const mergedInit = mergeRequestInit(init);
    return window.fetch(input, mergedInit)
      .then(response => {
        if (response.status === 401 || response.status === 403) {
  // login and redo
          return loginOn401().then(() => window.fetch(input, mergedInit))
        } else if (response.status === 404) {
          return response;
        } else if (!response.ok) {
          throw Error(response.statusText);
        }

        return response;
      });
  }
}


function mergeRequestInit(init: RequestInit | undefined): RequestInit | undefined {
  if (!init) {
    return init;
  }

  if (!init.method) {
    return init;
  }

  const method = init.method.toLocaleLowerCase();
  if (!(method === 'post' || method === 'put')) {
    return init;
  }

  const headers: HeadersInit = { ...(init.headers ?? {}) };
  if (!Object.keys(headers).map(e => e.toLowerCase()).includes('content-type')) {
    (headers as any)['content-type'] = 'application/json';
  }

  return { ...init, headers };
}
