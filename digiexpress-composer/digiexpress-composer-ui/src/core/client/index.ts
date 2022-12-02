import {
  ProgramStatus, ProgramMessage,
  Site, Entity, EntityId,
  CreateBuilder, InitSession,
  ServiceErrorMsg, ServiceErrorProps, Service, Store, DeleteBuilder,
  AstCommand,
  DocumentType, ConfigType,
  ServiceDocument,
  ServiceDefinitionDocument,
  ServiceRevisionDocument,
  ServiceReleaseDocument,
  ServiceConfigDocument,
  ServiceConfigValue
} from "./api";

import { StoreErrorImpl as StoreErrorImplAs, StoreError } from './error';
import { DefaultStore, StoreConfig } from './store';

declare namespace DigiexpressClient {
  export type {
    AstCommand,
    ProgramStatus, ProgramMessage,
    Site, Entity, EntityId,
    CreateBuilder, DeleteBuilder, InitSession,
    ServiceErrorMsg, ServiceErrorProps, Service, Store, StoreError, StoreConfig,
    DocumentType, ConfigType,
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
      return {} as any;
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

