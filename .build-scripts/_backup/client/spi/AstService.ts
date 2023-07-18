import AstAPI from '../AstAPI';
import { Store } from '../Service';

class ModelService implements AstAPI.Service {
  private _store: Store;
  constructor(store: Store) {
    this._store = store;
  }
    
  getParser(commands: AstAPI.ParserCommands): Promise<AstAPI.Parser> {
    return this._store.fetch("/commands", { method: "POST", body: JSON.stringify(commands) })
    .then(data => ({ type: commands.type, service: data as any }));
  }
}

export default ModelService;