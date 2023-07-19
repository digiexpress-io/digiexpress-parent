import * as API from './SecondaryAPI';



class SecondarySessionData implements API.SecondarySession {
  private _appId: string;
  private _secondary?: string;

  constructor(props: { appId: string, secondary?: string }) {
    this._appId = props.appId;
    this._secondary = props.secondary;
  }
  get appId() {
    return this._appId;
  }
  get secondary() {
    return this._secondary;
  }
  withSecondary(newItemId?: string): API.SecondarySession {
    return new SecondarySessionData({ appId: this._appId, secondary: newItemId });
  }
  withAppId(appId: string): API.SecondarySession {
    return new SecondarySessionData({ appId, secondary: this._secondary });
  }
}

export default SecondarySessionData;
