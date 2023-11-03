import { DocumentId, Document, DocumentUpdate, Session, PageUpdate, TabBody, TabEntity } from './composer-types';
import { HeadState } from './client-types';

class SiteCache {
  private _site: HeadState;
  constructor(site: HeadState) {
    this._site = site;
  }

  getEntity(entityId: DocumentId): Document {
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
    if (this._site.definitions[entityId]) {
      entity = this._site.definitions[entityId];
    }

    console.log("Retrieving entity from session", entityId, entity);
    return entity;
  }
}

class SessionData implements Session {
  private _head: HeadState;
  private _pages: Record<DocumentId, PageUpdate>;
  private _cache: SiteCache;

  constructor(props: {
    head?: HeadState;
    pages?: Record<DocumentId, PageUpdate>;
    cache?: SiteCache;
  }) {
    this._head = props.head ? props.head : { name: "", contentType: "OK", projects: {}, definitions: {}, releases: {} };
    this._pages = props.pages ? props.pages : {};
    this._cache = props.cache ? props.cache : new SiteCache(this._head);
  }
  get head() {
    return this._head;
  }
  get pages() {
    return this._pages;
  }
  getEntity(entityId: DocumentId): Document | undefined {
    return this._cache.getEntity(entityId);
  }
  withHead(head: HeadState) {
    if (!head) {
      console.error("Head not defined error", head);
      return this;
    }
    return new SessionData({ head, pages: this._pages });
  }
  withoutPages(pageIds: DocumentId[]): SessionData {
    const pages: Record<DocumentId, PageUpdate> = {};
    for (const page of Object.values(this._pages)) {
      if (pageIds.includes(page.origin.id)) {
        continue;
      }
      pages[page.origin.id] = page;
    }
    return new SessionData({ head: this._head, pages, cache: this._cache });
  }
  withPage(page: DocumentId): SessionData {
    if (this._pages[page]) {
      return this;
    }
    const pages = Object.assign({}, this._pages);
    const origin = this._cache.getEntity(page);


    if (!origin) {
      throw new Error("Can't find entity with id: '" + page + "'")
    }

    pages[page] = new ImmutablePageUpdate({ origin, saved: true, value: [] });
    return new SessionData({ head: this._head, pages, cache: this._cache });
  }
  withPageValue(page: DocumentId, value: DocumentUpdate[]): SessionData {
    const session = this.withPage(page);
    const pageUpdate = session.pages[page];

    const pages = Object.assign({}, session.pages);
    pages[page] = pageUpdate.withValue(value);

    return new SessionData({ head: session.head, pages, cache: this._cache });
  }
}

class ImmutablePageUpdate implements PageUpdate {
  private _saved: boolean;
  private _origin: Document;
  private _value: DocumentUpdate[];

  constructor(props: {
    saved: boolean;
    origin: Document;
    value: DocumentUpdate[];
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
  withValue(value: DocumentUpdate[]): PageUpdate {
    return new ImmutablePageUpdate({ saved: false, origin: this._origin, value });
  }
}

class ImmutableTabData implements TabBody {
  private _nav: TabEntity;

  constructor(props: { nav: TabEntity }) {
    this._nav = props.nav;
  }
  get nav() {
    return this._nav;
  }
  withNav(nav: TabEntity) {
    return new ImmutableTabData({
      nav: {
        value: nav.value === undefined ? this._nav.value : nav.value
      }
    });
  }
}



const initSession = new SessionData({});
export { SessionData, ImmutableTabData, initSession };
