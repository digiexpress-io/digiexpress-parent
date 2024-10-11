import { BurgerApi } from '../BurgerApi';

class AppSessionData implements BurgerApi.AppSession {
  private _active: BurgerApi.AppId;
  private _history: BurgerApi.AppState<any>[];

  constructor(props: {
    active: BurgerApi.AppId;
    history?: BurgerApi.AppState<any>[];
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
  withAppState(newState: BurgerApi.AppState<any>): BurgerApi.AppSession {
    const history: BurgerApi.AppState<any>[] = [];
    for (const value of this._history) {
      if (newState.id === value.id) {
        history.push(newState);
      } else {
        history.push(value);
      }
    }
    return new AppSessionData({ active: this._active, history });
  }
  withActive(active: BurgerApi.AppId): BurgerApi.AppSession {
    return new AppSessionData({ active, history: this._history });
  }
}
export default AppSessionData;