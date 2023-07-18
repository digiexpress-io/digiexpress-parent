import ResourceAPI from '../ResourceAPI';
import ModelAPI from '../ModelAPI';

import { Store } from '../Service';


class ResourceService implements ResourceAPI.Service {
  private _store: Store;

  constructor(store: Store) {
    this._store = store;
  }

  export(): Promise<string> {
    return this._store.fetch(`/exports`).then(data => JSON.stringify(data));
  }

  get(id: string): Promise<ResourceAPI.Asset> {
    return this._store.fetch(`/resources/${id}`);
  }
  update(asset: ResourceAPI.Asset, body: string): Promise<ResourceAPI.Asset> {
    return this._store.fetch(`/resources/${asset.id}`, { method: "PUT", body: body }).then((data) => {
      return data as any;
    })
  }
  create(name: string, type: ModelAPI.ServiceType): Promise<ResourceAPI.Asset> {
    const body = { name, type, content: "" };
    return this._store.fetch(`/resources`, { method: "POST", body: JSON.stringify(body) }).then((data) => {
      return data as any;
    })
  }
  delete(id: string): Promise<void> {
    return this._store.fetch(`/resources/${id}`, { method: "DELETE" }).then((data) => {
      return data as any;
    })
  }
  copy(id: string, name: string): Promise<ResourceAPI.Asset> {
    const body = { id, name };
    return this._store.fetch(`/copyas`, { method: "POST", body: JSON.stringify(body) }).then((data) => {
      return data as any;
    })
  }
}

export default ResourceService;