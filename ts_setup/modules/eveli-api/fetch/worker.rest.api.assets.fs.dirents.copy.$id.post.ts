import { createFileFetch } from '@dxs-ts/envir-fetch';
import { useIntl } from 'react-intl';
import { useSnackbar } from 'notistack';
import React from 'react';

export const Hook = createFileFetch('worker/rest/api/assets/fs/dirents/copy/$id.POST')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;

  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();

  const baseline = React.useCallback(async (id: string, body: {}): Promise<string> => {
    return params.fetch(url({ id }), {
      method,
      headers: { 'Accept': 'application/json', 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    })
    .then(resp => resp.json())
    .then((data: { id: string }) => data.id)
    .catch(error => {
      enqueueSnackbar(intl.formatMessage({ id: 'error.saveFailed' }, { cause: (error.message || 'N/A') }), { variant: 'error' });
      throw error;
    });
  }, [params]);

  return {
    postCopy: async (props: { id: string, newObjectName: string }): Promise<string> => {
      return baseline(props.id, { idOfObjectToCopy: props.id, newObjectName: props.newObjectName });
    }
  }
}
