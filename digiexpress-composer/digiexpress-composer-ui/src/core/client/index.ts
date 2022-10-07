import {
  ProgramStatus, ProgramMessage,
  Site, Entity, EntityId, 
  CreateBuilder, AstCommand, InitSession,
  ServiceErrorMsg, ServiceErrorProps, Service, Store, DeleteBuilder
} from "./api";

import { StoreErrorImpl as StoreErrorImplAs, StoreError } from './error';
import { DefaultStore, StoreConfig } from './store';

declare namespace DigiexpressClient {
  export type {
    ProgramStatus, ProgramMessage,
    Site, Entity, EntityId,
    AstCommand,
    CreateBuilder, DeleteBuilder, InitSession,
    ServiceErrorMsg, ServiceErrorProps, Service, Store, StoreError, StoreConfig,
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
  
    create(): DigiexpressClient.CreateBuilder {
      return { } as any;
    }
    delete(): DigiexpressClient.DeleteBuilder {
      return { } as any;
    }
    getSite(): Promise<DigiexpressClient.Site> {
      return { } as any;
    }
    copy(id: string, name: string): Promise<DigiexpressClient.Site> {
      return {} as any;
    }
  }
}



export default DigiexpressClient;

