import { StoreErrorImpl } from './error-types';
import { Store, StoreConfig, RepoType } from './backend-types';

import LoggerFactory from 'logger';
const log = LoggerFactory.getLogger();


export class BackendStoreImpl implements Store {
  private _config: StoreConfig;
  private _updateStarted: boolean = false;
  private _iapSessionRefreshWindow: Window | null = null;
  private _defRef: RequestInit;
  private _urls: Record<RepoType, string>;

  constructor(config: StoreConfig) {
    this._config = config;
    this._urls = { ... config.urls};
    const headers = {
      "Content-Type": "application/json;charset=UTF-8",
    };

    this._defRef = {
      method: "GET",
      credentials: 'same-origin',
      keepalive: true,
      headers
    }
    if (this._config.csrf) {
      const headers: Record<string, string> = this._defRef.headers as any;
      headers[this._config.csrf.key] = this._config.csrf.value;
    }
    log.target(config).debug("Composer::init DefaultStore");
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

  resolveUrl(props: { path: string, repoType: RepoType }): string {
    if (props.repoType === 'HEALTH') {
      return this._urls['HEALTH'];
    }
    return this._urls[props.repoType];
  }

  resolveRequest(repoType: RepoType): RequestInit {

    if (repoType === 'HEALTH') {
      return this._defRef;
    }
    if (repoType === 'CONFIG') {
      return this._defRef;
    }
    if (repoType === 'TENANT') {
      return this._defRef;
    }

    const next = { ...this._defRef, headers: { ...this._defRef.headers } };

    // @ts-ignore
    // next.headers['Project-ID'] = this._repos[repoType].repoId;

    return next;
  }

  async fetch<T>(path: string, req: RequestInit & { notFound?: () => T, repoType: RepoType }): Promise<T> {
    if (!path) {
      throw new Error("can't fetch with undefined url")
    }

    const url: string = this.resolveUrl({ path, repoType: req.repoType });
    const finalInit: RequestInit & { notFound?: () => T } = Object.assign({}, this.resolveRequest(req.repoType), req ? req : {});
    const response: Response | undefined = await fetch(url + path, finalInit);
    if (!response) {
      throw new StoreErrorImpl({
        text: `Response error, urls: ${url}, path: ${path}`,
        status: 500,
        errors: []
      });
    }

    if (response.status === 404) {
      if (finalInit.notFound) {
        console.error("404", path);
        return finalInit.notFound();
      }
      throw Error(`Response error, status: ${response.status}, statusText: ${response.statusText}, url: ${url}, path: ${path}`)
    }

    if (response.status === 302) {
      console.error("302", path);
      throw Error(`Response error, status: ${response.status}, statusText: ${response.statusText}`);
    }

    if (response.status === 401) {
      console.error("401", path);
      return this.handle401().then(() => fetch(url + path, finalInit))
        .then(response_1 => {
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
        });
    }
    if (response.status === 204) {
      return {} as any;
    }

    if (!response.ok) {
      console.error("Not ok", path);
      const data_1 = await response.json();
      console.error(data_1);
      throw new StoreErrorImpl({
        text: response.statusText,
        status: response.status,
        errors: data_1
      });
    }

    const text = await response.json()
    return text;

  }
}