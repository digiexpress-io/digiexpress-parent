import { createFileFetch } from '@dxs-ts/envir-fetch';
import { useIntl } from 'react-intl';
import { useSnackbar } from 'notistack';
import React from 'react';
import { Fs } from '@dxs-ts/fs-api';

export const Hook = createFileFetch('worker/rest/api/assets/fs/dirents.POST')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;

  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();

  const baseline = React.useCallback(async (assetUrl: string, assetBody: {}): Promise<string> => {
    return params.fetch(`${url({})}/${assetUrl}`, {
      method,
      headers: { 'Accept': 'application/json', 'Content-Type': 'application/json' },
      body: JSON.stringify(assetBody)
    })
    .then(resp => resp.json())
    .then((data: { id: string }) => data.id)
    .catch(error => {
      enqueueSnackbar(intl.formatMessage({ id: 'error.saveFailed' }, { cause: (error.message || 'N/A') }), { variant: 'error' });
      throw error;
    });
  }, [params]);

  return {
    postAny: async (props: { bodyType: Fs.BodyType; changes: Record<string, any> }): Promise<string> => {
      if (props.bodyType === 'ARTICLE_LINK') {
        return baseline('links', props.changes);
      }
      throw new Error(`postAny: unsupported bodyType ${props.bodyType}`);
    }
  }
}
