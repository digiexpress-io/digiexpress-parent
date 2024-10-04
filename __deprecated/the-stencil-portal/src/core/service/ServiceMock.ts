import * as Api from './Service';

import MockSites from './MockSites';
import DefaultFallbackSites from './FallbackSites';


interface Store {
  fetch<T>(url: string, init?: RequestInit): Promise<T>;
}


class ServiceMock implements Api.Service, Store {

  private _config: Api.ServiceConfig;
  private _fallback: Api.FallbackSites;

  constructor(config: Api.ServiceConfig) {
    this._config = config;
    this._fallback = config.fallbackSites ? config.fallbackSites : DefaultFallbackSites();
    console.log("using mock services", config);
  }


  async getSite(locale: string): Promise<Api.Site> {
    if(this._config.content.predefined) {
      return this._config.content.predefined;
    }
    
    return MockSites(locale).content;
    /*
    return this.fetch<Api.Site>(this._config.content.url + "?locale=" + locale)
      .then(site => {
        if (site) {
          return site;
        }
        return FallbackSites(locale).maintainance;
      });
      */
  }
  getSiteLoading(locale: string): Api.Site {
    const basedOnLocale = this._fallback.loading[locale];
    if(basedOnLocale) {
      return basedOnLocale;
    }
    return Object.values(this._fallback.loading)[0];
  }


  setErrors(value: any, url: string, init?: RequestInit) {
    console.error(value, url, init)
  }
  fetch<T>(url: string, init?: RequestInit): Promise<T> {
    if (!url) {
      throw new Error("can't fetch with undefined url")
    }

    const finalInit = init ? init : { method: "GET" };
    return fetch(url, finalInit)
      .then(response => {
        if (response.status === 302) {
          return null;
        }
        if (!response.ok) {
          throw new Error(response.statusText)
        }
        return response.json()
      })
      .catch(errors => this.setErrors(errors, url, init))
  }
}

export { ServiceMock };

