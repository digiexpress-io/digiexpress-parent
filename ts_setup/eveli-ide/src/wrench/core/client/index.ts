import { HdesApi } from "./api";
import { parseErrors } from "./error";

declare namespace HdesClient {

}

namespace HdesClient {

  export class StoreErrorImpl extends Error {
    private _props: HdesApi.ServiceErrorProps;
    constructor(props: HdesApi.ServiceErrorProps) {
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


export class DefaultStore implements HdesApi.Store {
  private _config: HdesApi.StoreConfig;
  private _updateStarted: boolean = false;
  private _iapSessionRefreshWindow: Window | null = null;
  private _defRef: RequestInit;

  constructor(config: HdesApi.StoreConfig) {
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


  
  export class ServiceImpl implements HdesApi.Service {
    private _store: HdesApi.Store;
    private _branch: string | undefined;
    private _headers: HeadersInit = {};

    constructor(store: HdesApi.Store, branchName?: string) {
      this._store = store;
      if (branchName) {
        if (branchName === "default") {
          this._branch = undefined;
          this._headers = {};
        } else {
          this._branch = branchName;
          // @ts-ignore
          this._headers["Branch-Name"] = branchName;
        }
      }
      // @ts-ignore
      this._headers["Content-Type"] = "application/json;charset=UTF-8";
    }
    withBranch(branchName?: string): ServiceImpl {
      return new ServiceImpl(this._store, branchName);
    }
    get branch(): string | undefined {
      return this._branch;
    }
    create(): HdesApi.CreateBuilder {
      const flow = (name: string) => this.createAsset(name, undefined, "FLOW");
      const service = (name: string) => this.createAsset(name, undefined, "FLOW_TASK");
      const decision = (name: string) => this.createAsset(name, undefined, "DT");
      const branch = (body: HdesApi.AstCommand[]) => this.createAsset("branch", undefined, "BRANCH", body);
      const tag = (props: {name: string, desc: string}) => this.createAsset(props.name, props.desc, "TAG");
      const site = () => this.createAsset("repo", undefined, "SITE");
      
      const importData = (tagContentAsString: string): Promise<HdesApi.Site> => {
        return this._store.fetch("/importTag", { method: "POST", body: tagContentAsString });
      }
      
      return { flow, service, decision, branch, site, tag, importData };
    }
    delete(): HdesApi.DeleteBuilder {
      const deleteMethod = (id: string): Promise<HdesApi.Site> => this._store.fetch(`/resources/${id}`, { method: "DELETE", headers: this._headers });
      const flow = (id: HdesApi.FlowId) => deleteMethod(id);
      const service = (id: HdesApi.ServiceId) => deleteMethod(id);
      const decision = (id: HdesApi.DecisionId) => deleteMethod(id);
      const branch = (id: HdesApi.BranchId) => deleteMethod(id);
      const tag = (id: HdesApi.TagId) => deleteMethod(id);
      return { flow, service, decision, tag, branch };
    }
    update(id: string, body: HdesApi.AstCommand[]): Promise<HdesApi.Site> {
      return this._store.fetch("/resources", { method: "PUT", body: JSON.stringify({ id, body }), headers: this._headers });
    }
    createAsset(name: string, desc: string | undefined, type: HdesApi.AstBodyType | "SITE", body?: HdesApi.AstCommand[]): Promise<HdesApi.Site> {
      return this._store.fetch("/resources", { method: "POST", body: JSON.stringify({ name, desc, type, body }), headers: this._headers });
    }
    ast(id: string, body: HdesApi.AstCommand[]): Promise<HdesApi.Entity<any>> {
      return this._store.fetch("/commands", { method: "POST", body: JSON.stringify({ id, body }), headers: this._headers });
    }
    getSite(): Promise<HdesApi.Site> {
      return this._store.fetch("/dataModels", { method: "GET", body: undefined, headers: this._headers }).then(data => {
        console.log(data);
        return data as HdesApi.Site;
      });
    }
    debug(debug: HdesApi.DebugRequest): Promise<HdesApi.DebugResponse> {
      return this._store.fetch("/debugs", { method: "POST", body: JSON.stringify(debug), headers: this._headers });
    }
    copy(id: string, name: string): Promise<HdesApi.Site> {
      return this._store.fetch("/copyas", { method: "POST", body: JSON.stringify({ id, name }), headers: this._headers });
    }
    version(): Promise<HdesApi.VersionEntity> {
      return this._store.fetch("/version", { method: "GET", body: undefined });
    }
    diff(input: HdesApi.DiffRequest): Promise<HdesApi.DiffResponse> {
      return this._store.fetch(`/diff?baseId=${input.baseId}&targetId=${input.targetId}`, { method: "GET", body: undefined });
    }
    summary(tagId: string): Promise<HdesApi.AstTagSummary> {
      return this._store.fetch(`/summary/${tagId}`, { method: "GET", body: undefined });
    }
  }
}


export type { HdesApi };
export default HdesClient;

