import { StoreErrorImpl } from './error-types';
import { Store, StoreConfig } from './client-types';



class DefaultStore implements Store {
  private _config: StoreConfig;
  private _updateStarted: boolean = false;
  private _iapSessionRefreshWindow: Window | null = null;
  private _defRef: RequestInit;

  constructor(config: StoreConfig) {
    this._config = config;
    this._defRef = {
      method: "GET",
      credentials: 'same-origin',
      headers: {
        "Content-Type": "application/json;charset=UTF-8"
      }
    }

    if (this._config.csrf) {
      const headers: Record<string, string> = this._defRef.headers as any;
      headers[this._config.csrf.key] = this._config.csrf.value;
    }

    console.log("Composer::init DefaultStore", config);
  }
  get config() {
    return this._config;
  }

  iapRefresh(): Promise<void> {
    return new Promise<void>((resolve, reject) => {
      // timeout in case login is required but not logged in
      setTimeout(() => reject(), 60000);
      const loop = () => {
        fetch(`${this._config.status}`).then((response) => {
          if (response.status === 401) {
            if (this._iapSessionRefreshWindow != null && !this._iapSessionRefreshWindow.closed) {
              setTimeout(loop, 1000);
            }
            else {
              this._iapSessionRefreshWindow = null;
              this._updateStarted = false;
              reject();
            }
          } else {
            this._iapSessionRefreshWindow?.close();
            this._iapSessionRefreshWindow = null;
            this._updateStarted = false;
            resolve();
          }
        });
      }
      if (this._iapSessionRefreshWindow != null && !this._iapSessionRefreshWindow.closed) {
        setTimeout(loop, 1000);
      } else {
        resolve();
      }
    });
  }

  iapLogin(): boolean {
    if (this._iapSessionRefreshWindow == null && !this._updateStarted) {
      this._updateStarted = true;
      const positionX = window.screenX + 30;
      const positionY = window.screenY + 30;
      this._iapSessionRefreshWindow = window.open(`${this._config.oidc}`, "_blank", `height=600,width=400,left=${positionX},top=${positionY}`);
    }
    return false;
  }

  handle401(): Promise<void> {
    this.iapLogin();
    return this.iapRefresh();
  }

  async fetch<T>(path: string, req?: RequestInit & { notFound?: () => T }): Promise<T> {
    if (!path) {
      throw new Error("can't fetch with undefined url")
    }
    const url = this._config.url;
    const finalInit: RequestInit & { notFound?: () => T } = Object.assign({}, this._defRef, req ? req : {});
    const response = await fetch(url + path, finalInit);
    if (response.status === 302) {
      throw Error(`Response error, status: ${response.status}, statusText: ${response.statusText}`);
    }
    if (response.status === 404) {
      if (finalInit.notFound) {
        return finalInit.notFound();
      }
      throw Error(`Response error, status: ${response.status}, statusText: ${response.statusText}`)
    }
    if (response.status === 401) {
      const response_1 = await this.handle401().then(() => fetch(url + path, finalInit));
      if (response_1.ok) {
        return response_1.json();
      }
      
      return response_1.json().then(data => {
        console.error(data);
        throw new StoreErrorImpl({
          text: response_1.statusText,
          status: response_1.status,
          errors: data
        });
      });
    }

    if (!response.ok) {
      return response.json().then(data_1 => {
        console.error(data_1);
        throw new StoreErrorImpl({
          text: response.statusText,
          status: response.status,
          errors: data_1
        });
      });
    }
    return response.json();
  }
};

export { DefaultStore };
