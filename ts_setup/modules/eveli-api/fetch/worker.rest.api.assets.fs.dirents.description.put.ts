import { createFileFetch } from '@dxs-ts/envir-fetch';
import { useIntl } from 'react-intl';
import { useSnackbar } from 'notistack';
import React from 'react';

export const Hook = createFileFetch('worker/rest/api/assets/fs/dirents/description.PUT')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;

  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();

  const baseline = React.useCallback(async (id: string, body: {}) => {
    return params.fetch(`${url({})}/${id}`, {
      method,
      headers: { 'Accept': 'application/json', 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    })
    .then((data) => console.log("update description:", data))
    .catch(error => {
      enqueueSnackbar(intl.formatMessage({ id: 'error.saveFailed' }, { cause: (error.message || 'N/A') }), { variant: 'error' });
    });
  }, [params]);

  return {
    putDescription: async (props: { id: string, text?: string }): Promise<void> => {
      await baseline(props.id, { id: props.id, text: props.text });
    }
  }
}
