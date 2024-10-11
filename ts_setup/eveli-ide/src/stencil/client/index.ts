import { StencilApi } from './StencilApi';
import createMock from './mock';
import createService from './impl';
import {parseErrors} from './error'

export type {StencilApi};

export namespace StencilClient {
  export const mock = (): StencilApi.Service => {
    return createMock();
  };
  export const service = (init: { store?: StencilApi.Store, config?: StencilApi.StoreConfig }): StencilApi.Service => {
    return createService(init);
  };



  export interface StoreError extends Error {
    text: string;
    status: number;
    errors: StencilApi.ErrorMsg[];
  }
  
  
  export class StoreErrorImpl extends Error {
    private _props: StencilApi.ErrorProps;
    constructor(props: StencilApi.ErrorProps) {
      super(props.text);
      this._props = {
        text: props.text,
        status: props.status,
        errors: parseErrors(props.errors)
      };
    }
    get name() {
      return this._props.text;
    }
    get status() {
      return this._props.status;
    }
    get errors() {
      return this._props.errors;
    }
  }

  export class DefaultStore implements StencilApi.Store {
    private _config: StencilApi.StoreConfig;
    private _updateStarted: boolean = false;
    private _iapSessionRefreshWindow: Window | null = null;
    private _defRef: RequestInit;

    constructor(config: StencilApi.StoreConfig) {
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

    fetch<T>(path: string, req?: RequestInit): Promise<T> {
      if (!path) {
        throw new Error("can't fetch with undefined url")
      }

      const url = this._config.url;
      const finalInit: RequestInit = Object.assign({}, this._defRef, req ? req : {});
      return fetch(url + path, finalInit)
        .then(response => {
          if (response.status === 302) {
            return null;
          }
          if (response.status === 401) {
            return this.handle401()
              .then(() => fetch(url + path, finalInit))
              .then(response => {
                if(response.ok) {
                  return response.json();
                }
                return response.json().then(data => {
                  console.error(data);
                  throw new StoreErrorImpl({
                    text: response.statusText,
                    status: response.status,
                    errors: data
                  });
                });
              });
          }

          if (!response.ok) {
            return response.json().then(data => {
              console.error(data);
              throw new StoreErrorImpl({
                text: response.statusText,
                status: response.status,
                errors: data
              });
            });
          }
          return response.json();
        })
    }
  };
}