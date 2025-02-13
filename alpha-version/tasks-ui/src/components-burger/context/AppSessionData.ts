import * as API from './AppAPI';

class AppSessionData implements API.AppSession {
  private _active: API.AppId;
  private _history: API.AppState<any>[];

  constructor(props: {
    active: API.AppId;
    history?: API.AppState<any>[];
  }) {
    this._active = props.active;
    this._history = props.history ? [...props.history] : [];
  }
  get history() {
    return this._history;
  }
  get active() {
    return this._active;
  }
  withAppState(newState: API.AppState<any>): API.AppSession {
    const history: API.AppState<any>[] = [];
    for (const value of this._history) {
      if (newState.id === value.id) {
        history.push(newState);
      } else {
        history.push(value);
      }
    }
    return new AppSessionData({ active: this._active, history });
  }
  withActive(active: API.AppId): API.AppSession {
    return new AppSessionData({ active, history: this._history });
  }
}
export default AppSessionData;