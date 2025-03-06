import { HdesApi } from "./api";
import { parseErrors } from "./error";

declare namespace HdesClient {

}

namespace HdesClient {


  
  export interface ServiceRestApi {
    update(id: string, body: HdesApi.AstCommand[]): Promise<HdesApi.Site>;
    createAsset(name: string, desc: string | undefined, type: HdesApi.AstBodyType | "SITE", body?: HdesApi.AstCommand[]): Promise<HdesApi.Site>;
    ast(id: string, body: HdesApi.AstCommand[]): Promise<HdesApi.Entity<any>>;
    getSite(): Promise<HdesApi.Site>;
    debug(debug: HdesApi.DebugRequest): Promise<HdesApi.DebugResponse>;
    copy(id: string, name: string): Promise<HdesApi.Site>;
    version(): Promise<HdesApi.VersionEntity>;
    diff(input: HdesApi.DiffRequest): Promise<HdesApi.DiffResponse>;
    summary(tagId: string): Promise<HdesApi.AstTagSummary>;
    remove(id: string): Promise<HdesApi.Site>;
    importTag(tagContentAsString: string): Promise<HdesApi.Site>;
  }

  export class ServiceImpl implements HdesApi.Service {
    private _api: ServiceRestApi;

    constructor(props: ServiceRestApi) {
      this._api = props;
    }
    create(): HdesApi.CreateBuilder {
      const flow = (name: string) => this.createAsset(name, undefined, "FLOW");
      const service = (name: string) => this.createAsset(name, undefined, "FLOW_TASK");
      const decision = (name: string) => this.createAsset(name, undefined, "DT");
      const branch = (body: HdesApi.AstCommand[]) => this.createAsset("branch", undefined, "BRANCH", body);
      const tag = (props: {name: string, desc: string}) => this.createAsset(props.name, props.desc, "TAG");
      const site = () => this.createAsset("repo", undefined, "SITE");
      
      const importData = (tagContentAsString: string): Promise<HdesApi.Site> => {
        return this._api.importTag(tagContentAsString)
      }
      
      return { flow, service, decision, branch, site, tag, importData };
    }
    delete(): HdesApi.DeleteBuilder {
      const deleteMethod = (id: string): Promise<HdesApi.Site> => this._api.remove(id);
      const flow = (id: HdesApi.FlowId) => deleteMethod(id);
      const service = (id: HdesApi.ServiceId) => deleteMethod(id);
      const decision = (id: HdesApi.DecisionId) => deleteMethod(id);
      const branch = (id: HdesApi.BranchId) => deleteMethod(id);
      const tag = (id: HdesApi.TagId) => deleteMethod(id);
      return { flow, service, decision, tag, branch };
    }


    update(id: string, body: HdesApi.AstCommand[]): Promise<HdesApi.Site> {
      return this._api.update(id, body);
    }
    createAsset(name: string, desc: string | undefined, type: HdesApi.AstBodyType | "SITE", body?: HdesApi.AstCommand[]): Promise<HdesApi.Site> {
      return this._api.createAsset(name, desc, type, body);
    }
    ast(id: string, body: HdesApi.AstCommand[]): Promise<HdesApi.Entity<any>> {
      return this._api.ast(id, body);
    }
    getSite(): Promise<HdesApi.Site> {
      return this._api.getSite();
    }
    debug(debug: HdesApi.DebugRequest): Promise<HdesApi.DebugResponse> {
      return this._api.debug(debug);
    }
    copy(id: string, name: string): Promise<HdesApi.Site> {
      return this._api.copy(id, name);
    }
    version(): Promise<HdesApi.VersionEntity> {
      return this._api.version();
    }
    diff(input: HdesApi.DiffRequest): Promise<HdesApi.DiffResponse> {
      return this._api.diff(input);
    }
    summary(tagId: string): Promise<HdesApi.AstTagSummary> {
      return this._api.summary(tagId);
    }
  }
}


export type { HdesApi };
export default HdesClient;

