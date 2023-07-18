import DebugAPI from '../DebugAPI';
import { Store } from '../Service';

class DebugService implements DebugAPI.Service {
  private _store: Store;
  constructor(store: Store) {
    this._store = store;
  }
    
  getDebug(commands: DebugAPI.DebugCommands): Promise<DebugAPI.Debug> {
    return this._store.fetch("/debugs", { method: "POST", body: JSON.stringify(commands) })
    .then(data => ({ type: commands.type, service: data as any }));
  }
}

export default DebugService;