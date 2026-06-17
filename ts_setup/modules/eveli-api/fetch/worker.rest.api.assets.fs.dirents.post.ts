import { createFileFetch } from '@dxs-ts/envir-fetch';
import React from 'react';
import { Fs } from '@dxs-ts/fs-api';

export const Hook = createFileFetch('worker/rest/api/assets/fs/dirents.POST')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;

  const baseline = React.useCallback(async (assetUrl: string, assetBody: {}): Promise<string> => {
    return params.fetch(`${url({})}/${assetUrl}`, {
      method,
      headers: { 'Accept': 'application/json', 'Content-Type': 'application/json' },
      body: JSON.stringify(assetBody)
    })
    .then(resp => resp.json())
    .then((data: { id: string }) => data.id);
  }, [params]);

  return {
    postAny: async (props: { bodyType: Fs.BodyType; changes: Record<string, any> }): Promise<string> => {
      if (props.bodyType === 'ARTICLE') {
        return baseline('articles', props.changes);
      }
      if (props.bodyType === 'ARTICLE_PAGE') {
        return baseline('article-page', props.changes);
      }
      if (props.bodyType === 'ARTICLE_LINK') {
        return baseline('links', props.changes);
      }
      if (props.bodyType === 'LOCALE') {
        return baseline('locales', props.changes);
      }
      if (props.bodyType === 'ARTICLE_WORKFLOW') {
        return baseline('article-workflows', props.changes);
      }
      if (props.bodyType === 'ARTICLE_TEMPLATE') {
        return baseline('article-templates', props.changes);
      }
      if (props.bodyType === 'FLOW') {
        return baseline('flows', props.changes);
      }
      if (props.bodyType === 'FLOW_TASK') {
        return baseline('flow-tasks', props.changes);
      }
      if (props.bodyType === 'DECISION_TABLE') {
        return baseline('decision-tables', props.changes);
      }
      if (props.bodyType === 'PRINTOUT') {
        return baseline('printouts', props.changes);
      }
      if (props.bodyType === 'PRINTOUT_RESOURCE') {
        return baseline('printout-resources', props.changes);
      }
      if (props.bodyType === 'PRINTOUT_PAGE') {
        return baseline('printout-pages', props.changes);
      }

      throw new Error(`postAny: unsupported bodyType ${props.bodyType}`);
    }
  }
}
