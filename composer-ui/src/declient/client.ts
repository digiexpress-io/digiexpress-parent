import { Client, Store, HeadState, DefinitionState, CreateBuilder, ServiceDefinitionId } from './client-types';
import {  } from './client-store';


  export class ServiceImpl implements Client {
    private _store: Store;

    constructor(store: Store) {
      this._store = store;
    }
    
    get config() {
      return this._store.config;
    }
    head(): Promise<HeadState> {
      return this._store.fetch<HeadState>("head", { notFound: () => ({
         name: "", contentType: "BACKEND_NOT_FOUND", projects: {}, definitions: {}, releases: {} 
      })})
      .catch((_error) => {
        const noConnection: HeadState = { name: "", contentType: "NO_CONNECTION", projects: {}, definitions: {}, releases: {} };
        return noConnection;
      });
    }
    create(): CreateBuilder {
      const head = () => this._store.fetch<HeadState>("head", { method: "POST", body: JSON.stringify({ }) });
      const migrate: (init: object) => Promise<HeadState> = (init) => this._store.fetch<HeadState>("migrate", { method: "POST", body: JSON.stringify(init) })
      
      return { head, migrate };
    }
    definition(id: ServiceDefinitionId): Promise<DefinitionState> {
      return this._store.fetch<DefinitionState>(`def/${id}`); 
    }
  }