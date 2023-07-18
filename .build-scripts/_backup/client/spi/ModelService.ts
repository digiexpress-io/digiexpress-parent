import ModelAPI from '../ModelAPI';
import { Store } from '../Service';

class ModelService implements ModelAPI.Service {
  private _store: Store;
  
  constructor(store: Store) {
    this._store = store;
  }
  
  getAbout(): Promise<ModelAPI.ServerAbout> {
    return {} as any;
  }
  find(): Promise<ModelAPI.Models> {
    return this._store.fetch("/dataModels");
  }
  findById(id: string): Promise<ModelAPI.Model> {
    return {} as any;    
  }
}

export default ModelService;