import { createFileFetch } from '@dxs-ts/envir-fetch';
import { useIntl } from 'react-intl';
import { useSnackbar } from 'notistack';
import { PublicationApi } from '../api-publications'




export const Hook = createFileFetch('worker/rest/api/assets/deployments/$deploymentId.GET')({
  hook
}) 


const downloadFile = ( data:any, fileName:string, fileType:string ) => {
  const blob = new Blob([data], { type: fileType })
  const a = document.createElement('a')
  a.download = fileName
  a.href = window.URL.createObjectURL(blob)
  const clickEvt = new MouseEvent('click', {
    view: window,
    bubbles: true,
    cancelable: true,
  })
  a.dispatchEvent(clickEvt)
  a.remove()
}

const handleErrors = (response:Response) => {
  if (!response.ok) {
      throw Error(response.statusText);
  }
  return response;
}


function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();

  return {
    getRelease: async (releaseTag: PublicationApi.Publication) => {
      return params.fetch(url({ deploymentId: releaseTag.name }), {
        headers: {
          'Accept': 'application/json'
        }
      })
      .then(response => handleErrors(response))
      .then((response: Response) => response.json())
      .then(json => {
        downloadFile(JSON.stringify(json, undefined, 2), releaseTag.name + '.json', 'text/json');
      })
      .catch(error => {
        enqueueSnackbar(intl.formatMessage({ id: 'assetRelease.downloadFailed' }, { cause: (error.message || 'N/A') }), { variant: 'error' });
      });
    }
  }
}