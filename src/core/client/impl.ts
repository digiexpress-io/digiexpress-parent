import StencilClient from './StencilClient';
import { DefaultStore } from './store';

const createService = (init: { store?: StencilClient.Store, config?: StencilClient.StoreConfig }): StencilClient.Service => {
  const backend: StencilClient.Store = init.config ? new DefaultStore(init.config) : init.store as any;

  const getSite: () => Promise<StencilClient.Site> = async () => backend.fetch("/").then((data) => data as any)
    .catch(resp => {

      // finish error handling

      const result: StencilClient.Site = {
        contentType: 'NO_CONNECTION',
        name: "not-connected",
        articles: {},
        links: {},
        locales: {},
        pages: {},
        releases: {},
        workflows: {},
        templates: {},
      };

      return result;
    })


  return {
    getSite,
    async getReleaseContent(releaseId: StencilClient.ReleaseId): Promise<{}> {
      return backend.fetch(`/releases/${releaseId}`, { method: "GET" }).then((data) => data as any);
    },
    create: () => new CreateBuilderImpl(backend),
    update: () => new UpdateBuilderImpl(backend),
    delete: () => new DeleteBuilderImpl(backend),
    version: () => new VersionBuilderImpl(backend)
  };
}

class CreateBuilderImpl implements StencilClient.CreateBuilder {
  private _backend: StencilClient.Store;
  constructor(backend: StencilClient.Store) {
    this._backend = backend;
  }
  async site(): Promise<StencilClient.Site> {
    return this._backend.fetch(`/`, { method: "POST" }).then((data) => data as any)
  }
  async importData(init: string): Promise<void> {
    return this._backend.fetch(`/migrations`, { method: "POST", body: init }).then((data) => data as any)
  }
  async release(init: StencilClient.CreateRelease): Promise<StencilClient.Release> {
    return this._backend.fetch(`/releases`, { method: "POST", body: JSON.stringify(init) }).then((data) => data as any)
  }
  async locale(init: StencilClient.CreateLocale): Promise<StencilClient.SiteLocale> {
    return this._backend.fetch(`/locales`, { method: "POST", body: JSON.stringify(init) }).then((data) => data as any)
  }
  async article(init: StencilClient.CreateArticle): Promise<StencilClient.Article> {
    return this._backend.fetch(`/articles`, { method: "POST", body: JSON.stringify(init) }).then((data) => data as any)
  }
  async page(init: StencilClient.CreatePage): Promise<StencilClient.Page> {
    return this._backend.fetch(`/pages`, { method: "POST", body: JSON.stringify(init) }).then((data) => data as any)
  }
  async link(init: StencilClient.CreateLink): Promise<StencilClient.Link> {
    return this._backend.fetch(`/links`, { method: "POST", body: JSON.stringify(init) }).then((data) => data as any)
  }
  async workflow(init: StencilClient.CreateWorkflow): Promise<StencilClient.Workflow> {
    return this._backend.fetch(`/workflows`, { method: "POST", body: JSON.stringify(init) }).then((data) => data as any)
  }
  async template(init: StencilClient.CreateTemplate): Promise<StencilClient.Template> {
    return this._backend.fetch(`/templates`, { method: "POST", body: JSON.stringify(init) }).then((data) => data as any)
  }
}

class UpdateBuilderImpl implements StencilClient.UpdateBuilder {
  private _backend: StencilClient.Store;
  constructor(backend: StencilClient.Store) {
    this._backend = backend;
  }
  async locale(init: StencilClient.LocaleMutator): Promise<StencilClient.SiteLocale> {
    return this._backend.fetch(`/locales`, { method: "PUT", body: JSON.stringify(init) }).then((data) => data as any)
  }
  async article(init: StencilClient.ArticleMutator): Promise<StencilClient.Article> {
    return this._backend.fetch(`/articles`, { method: "PUT", body: JSON.stringify(init) }).then((data) => data as any)
  }
  async pages(init: StencilClient.PageMutator[]): Promise<StencilClient.Page[]> {
    return this._backend.fetch(`/pages`, { method: "PUT", body: JSON.stringify(init) }).then((data) => data as any)
  }
  async link(init: StencilClient.LinkMutator): Promise<StencilClient.Link> {
    return this._backend.fetch(`/links`, { method: "PUT", body: JSON.stringify(init) }).then((data) => data as any)
  }
  async workflow(init: StencilClient.WorkflowMutator): Promise<StencilClient.Workflow> {
    return this._backend.fetch(`/workflows`, { method: "PUT", body: JSON.stringify(init) }).then((data) => data as any)
  }
  async template(init: StencilClient.TemplateMutator): Promise<StencilClient.Template> {
    return this._backend.fetch(`/templates`, { method: "PUT", body: JSON.stringify(init) }).then((data) => data as any)
  }
}

class DeleteBuilderImpl implements StencilClient.DeleteBuilder {
  private _backend: StencilClient.Store;
  constructor(backend: StencilClient.Store) {
    this._backend = backend;
  }
  async locale(init: StencilClient.LocaleId): Promise<void> {
    return this._backend.fetch(`/locales/${init}`, { method: "DELETE" }).then((data) => data as any)
  }
  async release(init: StencilClient.ReleaseId): Promise<void> {
    return this._backend.fetch(`/releases/${init}`, { method: "DELETE" }).then((data) => data as any)
  }
  async article(init: StencilClient.ArticleId): Promise<void> {
    return this._backend.fetch(`/articles/${init}`, { method: "DELETE" }).then((data) => data as any)
  }
  async page(init: StencilClient.PageId): Promise<void> {
    return this._backend.fetch(`/pages/${init}`, { method: "DELETE" }).then((data) => data as any)
  }
  async link(init: StencilClient.LinkId): Promise<void> {
    return this._backend.fetch(`/links/${init}`, { method: "DELETE" }).then((data) => data as any)
  }
  async workflow(init: StencilClient.WorkflowId): Promise<void> {
    return this._backend.fetch(`/workflows/${init}`, { method: "DELETE" }).then((data) => data as any)
  }
  async workflowArticlePage(workflow: StencilClient.WorkflowId, article: StencilClient.ArticleId, _locale: StencilClient.Locale): Promise<void> {
    return this._backend.fetch(`/workflows/${workflow}?articleId=${article}`, { method: "DELETE" }).then((data) => data as any)
  }
  async linkArticlePage(link: StencilClient.LinkId, article: StencilClient.ArticleId, _locale: StencilClient.Locale): Promise<void> {
    return this._backend.fetch(`/links/${link}?articleId=${article}`, { method: "DELETE" }).then((data) => data as any)
  }
  async template(init: StencilClient.TemplateId): Promise<void> {
    return this._backend.fetch(`/templates/${init}`, { method: "DELETE" }).then((data) => data as any)
  }
}

class VersionBuilderImpl implements StencilClient.VersionBuilder {
  private _backend: StencilClient.Store;
  constructor(backend: StencilClient.Store) {
    this._backend = backend;
  }
  async version(): Promise<StencilClient.VersionEntity> {
    return this._backend.fetch(`/version`, { method: "GET" }).then((data) => data as any)
  }
}

export default createService;

