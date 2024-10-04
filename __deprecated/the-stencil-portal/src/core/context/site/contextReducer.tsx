import * as Api from '../../service';
import { SiteActionOverrides, SiteActions } from './ContextTypes';
import SiteCache from './SiteCache';




interface SiteStateData {
  loaded: boolean;
  locale: string;
  site?: Api.Site;
  parent?: SiteState;
  views: Record<Api.TopicId, Api.TopicView>;
}

interface SiteState extends SiteStateData {
  withLocale(locale: string): SiteState;
  withSite(site: Api.Site): SiteState;
  getBlob(topic: Api.Topic): Api.Blob | undefined;
}

class ImmutableSiteState implements SiteState {
  private _locale: string;
  private _loaded: boolean;
  private _site?: Api.Site;
  private _parent?: SiteState;
  private _views: Record<Api.TopicId, Api.TopicView>;

  constructor(locale: string, init: {
    site?: Api.Site;
    parent?: SiteState;
  }) {
    this._locale = locale;
    this._loaded = init.site != null && init.site.loader !== true;
    this._site = init.site;
    this._parent = init.parent;
    this._views = init.site ? new SiteCache(init.site).topics : {};
  }
  get views() {
    return this._views;
  }
  getBlob(topic: Api.Topic) {
    if (!this._site) {
      return undefined;
    }
    if (!topic) {
      return undefined;
    }

    if (!topic.blob) {
      return undefined
    }
    return this._site.blobs[topic.blob];
  }
  withLocale(locale: string): SiteState {
    return new ImmutableSiteState(locale, this.init({ parent: this }));
  }
  withSite(site: Api.Site): SiteState {
    return new ImmutableSiteState(this._locale, this.init({ parent: this, site }));
  }
  init(arg: {}): SiteStateData {
    const newState: SiteStateData = {
      views: this._views,
      loaded: this._loaded,
      site: this._site,
      parent: this,
      locale: this._locale
    };
    return Object.assign(newState, arg);
  }
  get locale() {
    return this._locale;
  }
  get site() {
    return this._site;
  }
  get parent() {
    return this._parent;
  }
  get loaded() {
    return this._loaded;
  }
}

interface ContextAction {
  type: "setSite" | "setLocale",
  site?: Api.Site,
  locale?: string,
}


class SiteReducerDispatch implements SiteActions {

  private _sessionDispatch: React.Dispatch<ContextAction>;
  private _overrides: SiteActionOverrides;
  constructor(session: React.Dispatch<ContextAction>, overrides: SiteActionOverrides) {
    console.log("portal: init site dispatch");
    this._sessionDispatch = session;
    this._overrides = overrides;
  }
  setLocale(locale: string) {
    this._sessionDispatch({ type: "setLocale", locale })
  }
  setSite(site?: Api.Site, locale?: Api.LocaleCode) {
    const newSite = this._overrides.setSite ? this._overrides.setSite(site) : site;
    this._sessionDispatch({ type: "setSite", site: newSite, locale })
  }
}

const contextReducer = (oldState: SiteState, action: ContextAction): SiteState => {
  switch (action.type) {
    case "setSite": {
      if (!action.site) {
        console.error("new site is undefined");
        return oldState;
      }
      const site = action.site;      
      const newState = oldState.withSite(site).withLocale(action.locale ? action.locale : oldState.locale)
      return newState;
    }
    case "setLocale": {
      if (!action.locale) {
        console.error("new locale is undefined");
        return oldState;
      }
      if(action.locale === oldState.locale) {
        return oldState;
      }

      return oldState.withLocale(action.locale);
    }
  }
}


export type { SiteState, ContextAction };
export { contextReducer, ImmutableSiteState, SiteReducerDispatch };

