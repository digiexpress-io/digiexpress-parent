import { createFileFetch } from '@dxs-ts/envir-fetch';
import { useIntl } from 'react-intl';
import { useSnackbar } from 'notistack';
import React from 'react';
import { Fs } from '@dxs-ts/fs-api';

export const Hook = createFileFetch('worker/rest/api/assets/fs/dirents.PUT')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;

  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();

  const baseline = React.useCallback(async (assetUrl: string, assetBody: {}) => {

      return params.fetch(`${url({})}/${assetUrl}`, {
        method,
        headers: { 'Accept': 'application/json', 'Content-Type': 'application/json' },
        body: JSON.stringify(assetBody)
      })
      .then((data) => console.log("update asset:", data))
      .catch(error => {
        enqueueSnackbar(intl.formatMessage({ id: 'error.saveFailed' }, { cause: (error.message || 'N/A') }), { variant: 'error' });
      });
  }, [params]);

  return {
    putAny: async (props: { bodyType: Fs.BodyType; id: string, changes: Record<string, any> }): Promise<void> => {
      if (props.bodyType === 'ARTICLE_LINK') {
        await baseline(`links/${props.id}`, props.changes);
      }
      if (props.bodyType === 'ARTICLE') {
        await baseline(`articles/${props.id}`, props.changes);
      }
      if (props.bodyType === 'ARTICLE_PAGE') {
        await baseline(`article-page/${props.id}`, props.changes);
      }
    }
  }
}