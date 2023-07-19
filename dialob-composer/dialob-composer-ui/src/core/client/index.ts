import {
  ProgramStatus, ProgramMessage,
  Site, Entity, EntityId, DocumentType,
  FormId, ReleaseId, FormrevId, 
  FormRev, Form, Release, FormRevisionEntryDocument, 
  CreateBuilder, AstCommand, InitSession,
  ServiceErrorMsg, ServiceErrorProps, Service, Store, DeleteBuilder, ReleaseDump,
} from "./api";

import { StoreErrorImpl as StoreErrorImplAs, StoreError } from './error';
import { DefaultStore, StoreConfig } from './store';

declare namespace DialobClient {
  export type {
    ProgramStatus, ProgramMessage,
    Site, Entity, EntityId, DocumentType,
    FormrevId, FormId, ReleaseId, 
    FormRev, Form, Release, FormRevisionEntryDocument,
    AstCommand,
    CreateBuilder, DeleteBuilder, InitSession,
    ServiceErrorMsg, ServiceErrorProps, Service, Store, StoreError, StoreConfig,
  };
}

namespace DialobClient {
  export const StoreErrorImpl = StoreErrorImplAs;
  export const StoreImpl = DefaultStore;
  
  export class ServiceImpl implements DialobClient.Service {
    private _store: Store;

    constructor(store: DialobClient.Store) {
      this._store = store;
    }
    get config() {
      return this._store.config;
    }
    releaseDump(id: string): Promise<ReleaseDump> {
        throw new Error("Method not implemented.");
    }
  
    create(): DialobClient.CreateBuilder {
      const form = (name: string) => this.createAsset(name, undefined, "FORM");
      const release = (props: {name: string, desc: string}) => this.createAsset(props.name, props.desc, "RELEASE");
      const site = () => this.createAsset("repo", undefined, "SITE");
      const fill = (props: DialobClient.InitSession): Promise<{id: string}> => this._store.fetch("/sessions", { method: "POST", body: JSON.stringify(props) });
       
      const importData = (tagContentAsString: string): Promise<DialobClient.Site> => {
        return this._store.fetch("/importTag", { method: "POST", body: tagContentAsString });
      }
      
      return { form, site, release, importData, fill };
    }
    delete(): DialobClient.DeleteBuilder {
      const deleteMethod = (id: string): Promise<DialobClient.Site> => this._store.fetch(`/resources/${id}`, { method: "DELETE" });
      const form = (id: FormId) => deleteMethod(id);
      const release = (id: ReleaseId) => deleteMethod(id);
      const formrev = (id: FormrevId) => deleteMethod(id);
      
      return { form, release, formrev };
    }
    createAsset(name: string, desc: string | undefined, type: DialobClient.DocumentType | "SITE"): Promise<DialobClient.Site> {
      return this._store.fetch("/resources", { method: "POST", body: JSON.stringify({ name, desc, type }) });
    }
    getSite(): Promise<DialobClient.Site> {
      return this._store.fetch("/models", { method: "GET", body: undefined }).then(data => {
        
        
        return data as DialobClient.Site;
      });
    }
    copy(id: string, name: string): Promise<DialobClient.Site> {
      return this._store.fetch("/copyas", { method: "POST", body: JSON.stringify({ id, name }) });
    }
  }
}



export default DialobClient;

