import { createFileFetch } from '@dxs-ts/envir-fetch';

export const Hook = createFileFetch('dialob.GET')({
  hook
}) 

function hook(props: {}): { dialobUrl: string } {
  const params = Hook.useParams();
  const { url } = params;
 
  // direct dialob integration against FORM COMPOSER services
  return { dialobUrl: url({}) }
}