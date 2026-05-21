import { createFileFetch } from '@dxs-ts/envir-fetch';
  import { useIntl } from 'react-intl';
  import { useSnackbar } from 'notistack';

  export const Hook = createFileFetch('worker/rest/api/assets/fs/dirents/links/$id.PUT')({
    hook
  })

  function hook(props: {}) {
    const params = Hook.useParams();
    const { url, method } = params;

    const intl = useIntl();
    const { enqueueSnackbar } = useSnackbar();

    return {
      putLink: async (body: {
        linkId: string;
        value: string;
        type: string;
        articles: string[];
        labels: { locale: string; labelValue: string }[];
      }): Promise<void> => {
        return params.fetch(url({ id: body.linkId }), {
          method,
          headers: { 'Accept': 'application/json' },
          body: JSON.stringify(body)
        })
        .then((data) => console.log("update link data:", data))
        .catch(error => {
          enqueueSnackbar(intl.formatMessage({ id: 'error.saveFailed' }, { cause: (error.message || 'N/A') }), { variant: 'error' });
        });
      }
    }
  }