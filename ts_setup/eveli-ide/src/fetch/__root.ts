import { useIamForcedLogin } from '@/burger';
import { createRootFileFetch } from '@dxs-ts/eveli-fetch';


export const Hook = createRootFileFetch('', uberFetchWithAuthAndErrorHandling);


function uberFetchWithAuthAndErrorHandling()  {
  const { loginOn401 } = useIamForcedLogin();

  return (input: RequestInfo | URL, init?: RequestInit) => window.fetch(input, init)
    .then(response => {
      if (response.status === 401) {
        // login and redo
        return loginOn401().then(() => window.fetch(input, init))
      } else if(response.status === 404) {
        return response;
      } else if(!response.ok) {
        throw Error(response.statusText);
      }
      
      return response;
    })

}



