import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { Composer, StencilApi } from '../stencil';


export const Hook = createFileFetch('worker/rest/api/assets/stencil/$assetType.DELETE')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  
  return {
    delete: (): StencilApi.DeleteBuilder => ({
      locale: async (init: StencilApi.LocaleId): Promise<void> => {
        return params
          .fetch(url({ assetType: `locales/${init}`}), { method })
          .then(resp => resp.json())
      },
      release: async (init: StencilApi.ReleaseId): Promise<void> => {
        return params
          .fetch(url({ assetType: `releases/${init}`}), { method })
          .then(resp => resp.json())
      },
      article: async (init: StencilApi.ArticleId): Promise<void> => {
        return params
          .fetch(url({ assetType: `articles/${init}`}), { method })
          .then(resp => resp.json())
      },
      page: async (init: StencilApi.PageId): Promise<void> => {
        return params
          .fetch(url({ assetType: `pages/${init}`}), { method })
          .then(resp => resp.json())
      },
      link: async (init: StencilApi.LinkId): Promise<void> => {
        return params
          .fetch(url({ assetType: `links/${init}`}), { method })
          .then(resp => resp.json())
      },
      workflow: async (init: StencilApi.WorkflowId): Promise<void> => {
        return params
          .fetch(url({ assetType: `workflows/${init}`}), { method })
          .then(resp => resp.json())
      },
      workflowArticlePage: async (workflow: StencilApi.WorkflowId, article: StencilApi.ArticleId, _locale: StencilApi.Locale): Promise<void> => {
        return params
          .fetch(url({ assetType: `workflows/${workflow}?articleId=${article}`}), { method })
          .then(resp => resp.json())
      },
      linkArticlePage: async (link: StencilApi.LinkId, article: StencilApi.ArticleId, _locale: StencilApi.Locale): Promise<void> => {
        return params
          .fetch(url({ assetType: `links/${link}?articleId=${article}`}), { method })
          .then(resp => resp.json())
      },
      template: async (init: StencilApi.TemplateId): Promise<void> => {
        return params
          .fetch(url({ assetType: `templates/${init}`}), { method })
          .then(resp => resp.json())
      }
    })
  }
}