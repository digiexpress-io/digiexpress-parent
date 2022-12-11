import {
  ProgramStatus, ProgramMessage,
  Site, Entity, EntityId,
  CreateBuilder, InitSession,
  ServiceErrorMsg, ServiceErrorProps, Service, Store, DeleteBuilder,
  AstCommand,
  DocumentType, ConfigType,
  ServiceDocument,
  
  SiteDefinition,
  ServiceDefinitionDocument,
  ServiceRevisionDocument,
  ServiceReleaseDocument,
  ServiceConfigDocument,
  ServiceConfigValue,
  SiteMigrate,
  
  FormDocument, 
  ProcessValue, RefIdValue, ProcessValueId, ServiceDocumentId, 
  LocalizedSite,

} from "./api";

import { StoreErrorImpl as StoreErrorImplAs, StoreError } from './error';
import { DefaultStore, StoreConfig } from './store';

declare namespace DigiexpressClient {
  export type {
    LocalizedSite,
    FormDocument,
    ProcessValueId, ServiceDocumentId, 
    AstCommand,
    ProcessValue, RefIdValue,
    ProgramStatus, ProgramMessage,
    Site, Entity, EntityId,
    CreateBuilder, DeleteBuilder, InitSession,
    ServiceErrorMsg, ServiceErrorProps, Service, Store, StoreError, StoreConfig,
    SiteMigrate, 
    DocumentType, ConfigType, SiteDefinition,
    
    ServiceDocument,
    ServiceDefinitionDocument,
    ServiceRevisionDocument,
    ServiceReleaseDocument,
    ServiceConfigDocument,
    ServiceConfigValue
  };
}

namespace DigiexpressClient {
  export const StoreErrorImpl = StoreErrorImplAs;
  export const StoreImpl = DefaultStore;

  export class ServiceImpl implements DigiexpressClient.Service {
    private _store: Store;

    constructor(store: DigiexpressClient.Store) {
      this._store = store;
    }
    get config() {
      return this._store.config;
    }
    head(): Promise<DigiexpressClient.Site> {
      return this._store.fetch<DigiexpressClient.Site>("head", { notFound: () => ({
         name: "", contentType: "BACKEND_NOT_FOUND", revisions: {}, definitions: {}, configs: {}, releases: {} 
      })})
      .catch((_error) => {
        const noConnection: DigiexpressClient.Site = { name: "", contentType: "NO_CONNECTION", revisions: {}, definitions: {}, configs: {}, releases: {} };
        return noConnection;
      });
    }
    create(): DigiexpressClient.CreateBuilder {
      const site = () => this._store.fetch<DigiexpressClient.Site>("head", { method: "POST", body: JSON.stringify({ }) });
      const migrate: (init: SiteMigrate) => Promise<DigiexpressClient.Site> = (init) => this._store.fetch<DigiexpressClient.Site>("migrate", { method: "POST", body: JSON.stringify(init) })
      const release:(props: {name: string, desc: string}) => Promise<DigiexpressClient.Site> = () => {
        return {} as any;
      };
      
      return { site, migrate, release };
    }
    definition(id: ServiceDocumentId): Promise<DigiexpressClient.SiteDefinition> {
      return this._store.fetch<DigiexpressClient.SiteDefinition>(`def/${id}`); 
    }
    delete(): DigiexpressClient.DeleteBuilder {
      return {} as any;
    }
    getSite(): Promise<DigiexpressClient.Site> {
      return {} as any;
    }
    copy(id: string, name: string): Promise<DigiexpressClient.Site> {
      return {} as any;
    }
  }
}



export default DigiexpressClient;

