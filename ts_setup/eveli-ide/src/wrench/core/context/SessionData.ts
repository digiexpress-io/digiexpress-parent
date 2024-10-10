import Composer from './ide';
import Client from '../client';

class SiteCache {
  private _site: Client.Site;
  private _decisions: Record<string, Client.Entity<Client.AstDecision>> = {};
  private _flows: Record<string, Client.Entity<Client.AstFlow>> = {};
  private _services: Record<string, Client.Entity<Client.AstService>> = {};

  constructor(site: Client.Site) {
    this._site = site;
    
    Object.values(site.decisions).forEach(d => this.visitDecision(d))
    Object.values(site.services).forEach(d => this.visitService(d))
    Object.values(site.flows).forEach(d => this.visitFlow(d))
  }

  private visitFlow(flow: Client.Entity<Client.AstFlow>) {
    const { ast } = flow;
    if (!ast) {
      return;
    }
    this._flows[ast.name] = flow;
  }

  private visitDecision(decision: Client.Entity<Client.AstDecision>) {
    const { ast } = decision;
    if (!ast) {
      return;
    }
    this._decisions[ast.name] = decision;
  }

  private visitService(service: Client.Entity<Client.AstService>) {
    const { ast } = service;
    if (!ast) {
      return;
    }
    this._services[ast.name] = service;
  }

  getEntity(entityId: Client.EntityId): Client.Entity<any> {
    let entity: Client.Entity<any> = this._site.decisions[entityId];
    if (!entity) {
      entity = this._site.flows[entityId];
    }
    if (!entity) {
      entity = this._site.services[entityId];
    }
    if (!entity) {
      entity = this._site.tags[entityId];
    }
    if (!entity) {
      entity = this._site.branches[entityId];
    }
    return entity;
  }
  getDecision(decisionName: string): undefined | Client.Entity<Client.AstDecision> {
    return this._decisions[decisionName];
  }
  getFlow(flowName: string): undefined | Client.Entity<Client.AstFlow> {
    return this._flows[flowName];
  }
  getService(serviceName: string): undefined | Client.Entity<Client.AstService> {
    return this._services[serviceName];
  }
}

class SessionData implements Composer.Session {
  private _site: Client.Site;
  private _pages: Record<Client.EntityId, Composer.PageUpdate>;
  private _cache: SiteCache;
  private _debug: Composer.DebugSessions;
  private _branchName?: string;
  
  constructor(props: {
    site?: Client.Site;
    pages?: Record<Client.EntityId, Composer.PageUpdate>;
    cache?: SiteCache;
    debug?: Composer.DebugSessions;
    branchName?: string;
  }) {
    this._site = props.site ? props.site : { name: "", contentType: "OK", tags: {}, flows: {}, decisions: {}, services: {}, branches: {} };
    this._pages = props.pages ? props.pages : {};
    this._cache = props.cache ? props.cache : new SiteCache(this._site);
    this._debug = props.debug ? props.debug : { values: {}};
    this._branchName = props.branchName ? props.branchName : undefined;
  }
  get site() {
    return this._site;
  }
  get pages() {
    return this._pages;
  }
  get debug() {
    return this._debug;
  }
  get branchName() {
    return this._branchName;
  }
  getDecision(decisionName: string): undefined | Client.Entity<Client.AstDecision> {
    return this._cache.getDecision(decisionName);
  }
  getFlow(flowName: string): undefined | Client.Entity<Client.AstFlow> {
    return this._cache.getFlow(flowName);
  }
  getService(serviceName: string): undefined | Client.Entity<Client.AstService> {
    return this._cache.getService(serviceName);
  }
  getEntity(entityId: Client.EntityId): Client.Entity<any> | undefined {
    return this._cache.getEntity(entityId);
  }
  withSite(site: Client.Site) {
    return new SessionData({ site: site, pages: this._pages, debug: this._debug, branchName: this._branchName });
  }
  withDebug(debugSession: Composer.DebugSession) {
    const newDebug: Record<Client.EntityId, Composer.DebugSession> = {};
    newDebug[debugSession.selected] = Object.assign({}, debugSession);
    const debug: Composer.DebugSessions = {
      selected: debugSession.selected,
      values: Object.assign({}, this._debug.values, newDebug)
    }
    return new SessionData({ site: this._site, pages: this._pages, cache: this._cache, debug, branchName: this._branchName });
  }
  withBranch(branchName: string): Composer.Session {
    return new SessionData({ site: this._site, pages: this._pages, cache: this._cache, debug: this._debug, branchName });
  }
  withoutPages(pageIds: Client.EntityId[]): Composer.Session {
    const pages = {};
    for (const page of Object.values(this._pages)) {
      if (pageIds.includes(page.origin.id)) {
        continue;
      }
      pages[page.origin.id] = page;
    }
    return new SessionData({ site: this._site, pages, cache: this._cache, debug: this._debug, branchName: this._branchName });
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
    return new SessionData({ site: this._site, pages, cache: this._cache, debug: this._debug, branchName: this._branchName });
  }
  withPageValue(page: Client.EntityId, value: Client.AstCommand[]): Composer.Session {
    const session = this.withPage(page);
    const pageUpdate = session.pages[page];

    const pages = Object.assign({}, session.pages);
    pages[page] = pageUpdate.withValue(value);

    return new SessionData({ site: session.site, pages, cache: this._cache, debug: this._debug, branchName: this._branchName });
  }
}

class ImmutablePageUpdate implements Composer.PageUpdate {
  private _saved: boolean;
  private _origin: Client.Entity<any>;
  private _value: Client.AstCommand[];

  constructor(props: {
    saved: boolean;
    origin: Client.Entity<any>;
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
