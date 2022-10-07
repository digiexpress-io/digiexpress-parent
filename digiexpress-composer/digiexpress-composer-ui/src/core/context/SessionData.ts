import Composer from './ide';
import Client from '../client';

class SiteCache {
  private _site: Client.Site;

  constructor(site: Client.Site) {
    this._site = site;
    
    
  }

  getEntity(entityId: Client.EntityId): Client.Entity {
    /*
    if(entityId.startsWith("debug-fill/")) {
      entityId = entityId.substring(11);
    }
    let entity: Client.Entity = this._site.forms[entityId];
    if (!entity) {
      entity = this._site.revs[entityId];
    }
    if (!entity) {
      entity = this._site.releases[entityId];
    }
    */
    var entity = {} as any;
    console.log("Retrieving entity from session", entityId, entity);
    return entity;
  }
}

class SessionData implements Composer.Session {
  private _site: Client.Site;
  private _pages: Record<Client.EntityId, Composer.PageUpdate>;
  private _cache: SiteCache;
  
  constructor(props: {
    site?: Client.Site;
    pages?: Record<Client.EntityId, Composer.PageUpdate>;
    cache?: SiteCache;
  }) {
    this._site = props.site ? props.site : { name: "", contentType: "OK" };
    this._pages = props.pages ? props.pages : {};
    this._cache = props.cache ? props.cache : new SiteCache(this._site);
  }
  get site() {
    return this._site;
  }
  get pages() {
    return this._pages;
  }
  getEntity(entityId: Client.EntityId): Client.Entity | undefined {
    return this._cache.getEntity(entityId);
  }
  withSite(site: Client.Site) {
    return new SessionData({ site: site, pages: this._pages });
  }

  withoutPages(pageIds: Client.EntityId[]): Composer.Session {
    const pages: Record<Client.EntityId, Composer.PageUpdate> = {};
    for (const page of Object.values(this._pages)) {
      if (pageIds.includes(page.origin.id)) {
        continue;
      }
      pages[page.origin.id] = page;
    }
    return new SessionData({ site: this._site, pages, cache: this._cache });
  }
  withPage(page: Client.EntityId): Composer.Session {
    if (this._pages[page]) {
      return this;
    }
    const pages = Object.assign({}, this._pages);
    const origin = this._cache.getEntity(page);


    if (!origin) {
      throw new Error("Can't find entity with id: '" + page + "'")
    }

    pages[page] = new ImmutablePageUpdate({ origin, saved: true, value: [] });
    return new SessionData({ site: this._site, pages, cache: this._cache });
  }
  withPageValue(page: Client.EntityId, value: Client.AstCommand[]): Composer.Session {
    const session = this.withPage(page);
    const pageUpdate = session.pages[page];

    const pages = Object.assign({}, session.pages);
    pages[page] = pageUpdate.withValue(value);

    return new SessionData({ site: session.site, pages, cache: this._cache });
  }
}

class ImmutablePageUpdate implements Composer.PageUpdate {
  private _saved: boolean;
  private _origin: Client.Entity;
  private _value: Client.AstCommand[];

  constructor(props: {
    saved: boolean;
    origin: Client.Entity;
    value: Client.AstCommand[];
  }) {
    this._saved = props.saved;
    this._origin = props.origin;
    this._value = props.value;
  }

  get saved() {
    return this._saved;
  }
  get origin() {
    return this._origin;
  }
  get value() {
    return this._value;
  }
  withValue(value: Client.AstCommand[]): Composer.PageUpdate {
    return new ImmutablePageUpdate({ saved: false, origin: this._origin, value });
  }
}

class ImmutableTabData implements Composer.TabData {
  private _nav: Composer.Nav;

  constructor(props: { nav: Composer.Nav }) {
    this._nav = props.nav;
  }
  get nav() {
    return this._nav;
  }
  withNav(nav: Composer.Nav) {
    return new ImmutableTabData({
      nav: {
        value: nav.value === undefined ? this._nav.value : nav.value
      }
    });
  }
}

export { SessionData, ImmutableTabData };
