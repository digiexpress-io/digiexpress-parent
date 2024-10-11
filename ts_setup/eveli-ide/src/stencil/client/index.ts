import { StencilApi } from './StencilApi';
import createMock from './mock';
import {parseErrors} from './error'

export type {StencilApi};

export namespace StencilClient {
  export const mock = (): StencilApi.Service => {
    return createMock();
  };
  export const service = (init: { store?: StencilApi.Store, config?: StencilApi.StoreConfig }): StencilApi.Service => {
    return createService(init);
  };

   const createService = (init: { store?: StencilApi.Store, config?: StencilApi.StoreConfig }): StencilApi.Service => {
    const backend: StencilApi.Store = init.config ? new DefaultStore(init.config) : init.store as any;
  
    const getSite: () => Promise<StencilApi.Site> = async () => backend.fetch("/").then((data) => data as any)
      .catch(resp => {
  
        // finish error handling
  
        const result: StencilApi.Site = {
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
  
    const version: () => Promise<StencilApi.VersionEntity> = async () => backend.fetch(`/version`, { method: "GET" })
      .then((data) => data as any);
  
    return {
      getSite,
      async getReleaseContent(releaseId: StencilApi.ReleaseId): Promise<{}> {
        return backend.fetch(`/releases/${releaseId}`, { method: "GET" }).then((data) => data as any);
      },
      create: () => new CreateBuilderImpl(backend),
      update: () => new UpdateBuilderImpl(backend),
      delete: () => new DeleteBuilderImpl(backend),
      version
    };
  }
  
  export class CreateBuilderImpl implements StencilApi.CreateBuilder {
    private _backend: StencilApi.Store;
    constructor(backend: StencilApi.Store) {
      this._backend = backend;
    }
    async site(): Promise<StencilApi.Site> {
      return this._backend.fetch(`/`, { method: "POST" }).then((data) => data as any)
    }
    async importData(init: string): Promise<void> {
      return this._backend.fetch(`/migrations`, { method: "POST", body: init }).then((data) => data as any)
    }
    async release(init: StencilApi.CreateRelease): Promise<StencilApi.Release> {
      return this._backend.fetch(`/releases`, { method: "POST", body: JSON.stringify(init) }).then((data) => data as any)
    }
    async locale(init: StencilApi.CreateLocale): Promise<StencilApi.SiteLocale> {
      return this._backend.fetch(`/locales`, { method: "POST", body: JSON.stringify(init) }).then((data) => data as any)
    }
    async article(init: StencilApi.CreateArticle): Promise<StencilApi.Article> {
      return this._backend.fetch(`/articles`, { method: "POST", body: JSON.stringify(init) }).then((data) => data as any)
    }
    async page(init: StencilApi.CreatePage): Promise<StencilApi.Page> {
      return this._backend.fetch(`/pages`, { method: "POST", body: JSON.stringify(init) }).then((data) => data as any)
    }
    async link(init: StencilApi.CreateLink): Promise<StencilApi.Link> {
      return this._backend.fetch(`/links`, { method: "POST", body: JSON.stringify(init) }).then((data) => data as any)
    }
    async workflow(init: StencilApi.CreateWorkflow): Promise<StencilApi.Workflow> {
      return this._backend.fetch(`/workflows`, { method: "POST", body: JSON.stringify(init) }).then((data) => data as any)
    }
    async template(init: StencilApi.CreateTemplate): Promise<StencilApi.Template> {
      return this._backend.fetch(`/templates`, { method: "POST", body: JSON.stringify(init) }).then((data) => data as any)
    }
  }
  
  export class UpdateBuilderImpl implements StencilApi.UpdateBuilder {
    private _backend: StencilApi.Store;
    constructor(backend: StencilApi.Store) {
      this._backend = backend;
    }
    async locale(init: StencilApi.LocaleMutator): Promise<StencilApi.SiteLocale> {
      return this._backend.fetch(`/locales`, { method: "PUT", body: JSON.stringify(init) }).then((data) => data as any)
    }
    async article(init: StencilApi.ArticleMutator): Promise<StencilApi.Article> {
      return this._backend.fetch(`/articles`, { method: "PUT", body: JSON.stringify(init) }).then((data) => data as any)
    }
    async pages(init: StencilApi.PageMutator[]): Promise<StencilApi.Page[]> {
      return this._backend.fetch(`/pages`, { method: "PUT", body: JSON.stringify(init) }).then((data) => data as any)
    }
    async link(init: StencilApi.LinkMutator): Promise<StencilApi.Link> {
      return this._backend.fetch(`/links`, { method: "PUT", body: JSON.stringify(init) }).then((data) => data as any)
    }
    async workflow(init: StencilApi.WorkflowMutator): Promise<StencilApi.Workflow> {
      return this._backend.fetch(`/workflows`, { method: "PUT", body: JSON.stringify(init) }).then((data) => data as any)
    }
    async template(init: StencilApi.TemplateMutator): Promise<StencilApi.Template> {
      return this._backend.fetch(`/templates`, { method: "PUT", body: JSON.stringify(init) }).then((data) => data as any)
    }
  }
  
  export class DeleteBuilderImpl implements StencilApi.DeleteBuilder {
    private _backend: StencilApi.Store;
    constructor(backend: StencilApi.Store) {
      this._backend = backend;
    }
    async locale(init: StencilApi.LocaleId): Promise<void> {
      return this._backend.fetch(`/locales/${init}`, { method: "DELETE" }).then((data) => data as any)
    }
    async release(init: StencilApi.ReleaseId): Promise<void> {
      return this._backend.fetch(`/releases/${init}`, { method: "DELETE" }).then((data) => data as any)
    }
    async article(init: StencilApi.ArticleId): Promise<void> {
      return this._backend.fetch(`/articles/${init}`, { method: "DELETE" }).then((data) => data as any)
    }
    async page(init: StencilApi.PageId): Promise<void> {
      return this._backend.fetch(`/pages/${init}`, { method: "DELETE" }).then((data) => data as any)
    }
    async link(init: StencilApi.LinkId): Promise<void> {
      return this._backend.fetch(`/links/${init}`, { method: "DELETE" }).then((data) => data as any)
    }
    async workflow(init: StencilApi.WorkflowId): Promise<void> {
      return this._backend.fetch(`/workflows/${init}`, { method: "DELETE" }).then((data) => data as any)
    }
    async workflowArticlePage(workflow: StencilApi.WorkflowId, article: StencilApi.ArticleId, _locale: StencilApi.Locale): Promise<void> {
      return this._backend.fetch(`/workflows/${workflow}?articleId=${article}`, { method: "DELETE" }).then((data) => data as any)
    }
    async linkArticlePage(link: StencilApi.LinkId, article: StencilApi.ArticleId, _locale: StencilApi.Locale): Promise<void> {
      return this._backend.fetch(`/links/${link}?articleId=${article}`, { method: "DELETE" }).then((data) => data as any)
    }
    async template(init: StencilApi.TemplateId): Promise<void> {
      return this._backend.fetch(`/templates/${init}`, { method: "DELETE" }).then((data) => data as any)
    }
  }
  export interface StoreError extends Error {
    text: string;
    status: number;
    errors: StencilApi.ErrorMsg[];
  }
  
  
  export class StoreErrorImpl extends Error {
    private _props: StencilApi.ErrorProps;
    constructor(props: StencilApi.ErrorProps) {
      super(props.text);
      this._props = {
        text: props.text,
        status: props.status,
        errors: parseErrors(props.errors)
      };
    }
    get name() {
      return this._props.text;
    }
    get status() {
      return this._props.status;
    }
    get errors() {
      return this._props.errors;
    }
  }

  export class DefaultStore implements StencilApi.Store {
    private _config: StencilApi.StoreConfig;
    private _updateStarted: boolean = false;
    private _iapSessionRefreshWindow: Window | null = null;
    private _defRef: RequestInit;

    constructor(config: StencilApi.StoreConfig) {
      this._config = config;
      this._defRef = {
        method: "GET",
        credentials: 'same-origin',
        headers: {
          "Content-Type": "application/json;charset=UTF-8"
        }
      }
      
      if (this._config.csrf) {
        const headers: Record<string, string> = this._defRef.headers as any;
        headers[this._config.csrf.key] = this._config.csrf.value;
      }
    }

    iapRefresh(): Promise<void> {
      return new Promise<void>((resolve, reject) => {
        // timeout in case login is required but not logged in
        setTimeout(() => reject(), 60000);
        const loop = () => {
          fetch(`${this._config.status}`).then((response) => {
            if (response.status === 401) {
              if (this._iapSessionRefreshWindow != null && !this._iapSessionRefreshWindow.closed) {
                setTimeout(loop, 1000);
              }
              else {
                this._iapSessionRefreshWindow = null;
                this._updateStarted = false;
                reject();
              }
            } else {
              this._iapSessionRefreshWindow?.close();
              this._iapSessionRefreshWindow = null;
              this._updateStarted = false;
              resolve();
            }
          });
        }
        if (this._iapSessionRefreshWindow != null && !this._iapSessionRefreshWindow.closed) {
          setTimeout(loop, 1000);
        } else {
          resolve();
        }
      });
    }

    iapLogin(): boolean {
      if (this._iapSessionRefreshWindow == null && !this._updateStarted) {
        this._updateStarted = true;
        const positionX = window.screenX + 30;
        const positionY = window.screenY + 30;
        this._iapSessionRefreshWindow = window.open(`${this._config.oidc}`, "_blank", `height=600,width=400,left=${positionX},top=${positionY}`);
      }
      return false;
    }

    handle401(): Promise<void> {
      this.iapLogin();
      return this.iapRefresh();
    }

    fetch<T>(path: string, req?: RequestInit): Promise<T> {
      if (!path) {
        throw new Error("can't fetch with undefined url")
      }

      const url = this._config.url;
      const finalInit: RequestInit = Object.assign({}, this._defRef, req ? req : {});
      return fetch(url + path, finalInit)
        .then(response => {
          if (response.status === 302) {
            return null;
          }
          if (response.status === 401) {
            return this.handle401()
              .then(() => fetch(url + path, finalInit))
              .then(response => {
                if(response.ok) {
                  return response.json();
                }
                return response.json().then(data => {
                  console.error(data);
                  throw new StoreErrorImpl({
                    text: response.statusText,
                    status: response.status,
                    errors: data
                  });
                });
              });
          }

          if (!response.ok) {
            return response.json().then(data => {
              console.error(data);
              throw new StoreErrorImpl({
                text: response.statusText,
                status: response.status,
                errors: data
              });
            });
          }
          return response.json();
        })
    }
  };
}