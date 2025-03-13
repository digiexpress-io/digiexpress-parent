import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { StencilApi } from '../stencil';


export const Hook = createFileFetch('worker/rest/api/assets/stencil/$assetType.PUT')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  
  return {
    update: (): StencilApi.UpdateBuilder => ({
      locale: async (init: StencilApi.LocaleMutator): Promise<StencilApi.SiteLocale> => {
        return params
          .fetch(url({ assetType: `locales`}), { method, body: JSON.stringify(init) })
          .then(resp => resp.json())
      },
      article: async (init: StencilApi.ArticleMutator): Promise<StencilApi.Article> => {
        return params
          .fetch(url({ assetType: `articles`}), { method, body: JSON.stringify(init) })
          .then(resp => resp.json())
      },
      pages: async (init: StencilApi.PageMutator[]): Promise<StencilApi.Page[]> => {
        return params
          .fetch(url({ assetType: `pages`}), { method, body: JSON.stringify(init) })
          .then(resp => resp.json())
      },
      link: async (init: StencilApi.LinkMutator): Promise<StencilApi.Link> => {
        return params
          .fetch(url({ assetType: `links`}), { method, body: JSON.stringify(init) })
          .then(resp => resp.json())
      },
      workflow: async (init: StencilApi.WorkflowMutator): Promise<StencilApi.Workflow> => {
        return params
          .fetch(url({ assetType: `workflows`}), { method, body: JSON.stringify(init) })
          .then(resp => resp.json())
      },
      template: async (init: StencilApi.TemplateMutator): Promise<StencilApi.Template> => {
        return params
          .fetch(url({ assetType: `templates`}), { method, body: JSON.stringify(init) })
          .then(resp => resp.json())
      }
    })
  }
}