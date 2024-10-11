import { HdesApi } from "./api";

import { StoreErrorImpl as StoreErrorImplAs } from './error';
import { DefaultStore } from './store';

declare namespace HdesClient {

}

namespace HdesClient {
  export const StoreErrorImpl = StoreErrorImplAs;
  export const StoreImpl = DefaultStore;
  
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

