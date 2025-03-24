import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { StencilApi } from '@/burger'


export const Hook = createFileFetch('worker/rest/api/assets/stencil/$assetType.POST')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  
  return {
    create: (): StencilApi.CreateBuilder => ({
      site: async (): Promise<StencilApi.Site> => {
        return params
          .fetch(url({ assetType: '/'}), { method })
          .then(resp => resp.json())
      },
      importData: async (init: string): Promise<void> => {
        return params
          .fetch(url({ assetType: `migrations`}), { method, body: init })
          .then(resp => resp.json())
      },
      release: async (init: StencilApi.CreateRelease): Promise<StencilApi.Release> => {
        return params
          .fetch(url({ assetType: `releases`}), { method, body: JSON.stringify(init) })
          .then(resp => resp.json())
      },
      locale: async (init: StencilApi.CreateLocale): Promise<StencilApi.SiteLocale> => {
        return params
          .fetch(url({ assetType: `locales`}), { method, body: JSON.stringify(init) })
          .then(resp => resp.json())
      },
      article: async(init: StencilApi.CreateArticle): Promise<StencilApi.Article> => {
        return params
          .fetch(url({ assetType: `articles`}), { method, body: JSON.stringify(init) })
          .then(resp => resp.json())
      },
      page: async(init: StencilApi.CreatePage): Promise<StencilApi.Page> => {
        return params
          .fetch(url({ assetType: `pages`}), { method, body: JSON.stringify(init) })
          .then(resp => resp.json())
      },
      link: async(init: StencilApi.CreateLink): Promise<StencilApi.Link> => {
        return params
          .fetch(url({ assetType: `links`}), { method, body: JSON.stringify(init) })
          .then(resp => resp.json())
      },
      workflow: async (init: StencilApi.CreateWorkflow): Promise<StencilApi.Workflow> => {
        return params
          .fetch(url({ assetType: `workflows`}), { method, body: JSON.stringify(init) })
          .then(resp => resp.json())
      },
      template: async (init: StencilApi.CreateTemplate): Promise<StencilApi.Template> => {
        return params
          .fetch(url({ assetType: `templates`}), { method, body: JSON.stringify(init) })
          .then(resp => resp.json())
      }
    })
  }
}