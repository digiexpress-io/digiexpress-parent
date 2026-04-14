import { WrenchComposerApi } from './ide';
import { HdesApi } from './api';

class SiteCache {
  private _site: HdesApi.Site;
  private _decisions: Record<string, HdesApi.Entity<HdesApi.AstDecision>> = {};
  private _flows: Record<string, HdesApi.Entity<HdesApi.AstFlow>> = {};
  private _services: Record<string, HdesApi.Entity<HdesApi.AstService>> = {};

  constructor(site: HdesApi.Site) {
    this._site = site;
    
    Object.values(site.decisions).forEach(d => this.visitDecision(d))
    Object.values(site.services).forEach(d => this.visitService(d))
    Object.values(site.flows).forEach(d => this.visitFlow(d))
  }

  private visitFlow(flow: HdesApi.Entity<HdesApi.AstFlow>) {
    const { ast } = flow;
    if (!ast) {
      return;
    }
    this._flows[ast.name] = flow;
  }

  private visitDecision(decision: HdesApi.Entity<HdesApi.AstDecision>) {
    const { ast } = decision;
    if (!ast) {
      return;
    }
    this._decisions[ast.name] = decision;
  }

  private visitService(service: HdesApi.Entity<HdesApi.AstService>) {
    const { ast } = service;
    if (!ast) {
      return;
    }
    this._services[ast.name] = service;
  }

  getEntity(entityId: HdesApi.EntityId): HdesApi.Entity<any> {
    let entity: HdesApi.Entity<any> = this._site.decisions[entityId];
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
  getDecision(decisionName: string): undefined | HdesApi.Entity<HdesApi.AstDecision> {
    return this._decisions[decisionName];
  }
  getFlow(flowName: string): undefined | HdesApi.Entity<HdesApi.AstFlow> {
    return this._flows[flowName];
  }
  getService(serviceName: string): undefined | HdesApi.Entity<HdesApi.AstService> {
    return this._services[serviceName];
  }
}

export class SessionData implements WrenchComposerApi.Session {
  private _site: HdesApi.Site;
  private _pages: Record<HdesApi.EntityId, WrenchComposerApi.PageUpdate>;
  private _cache: SiteCache;
  private _debug: WrenchComposerApi.DebugSessions;
  private _commitlogs: Record<string, HdesApi.CommitLog[]>;

  
  constructor(props: {
    site?: HdesApi.Site;
    pages?: Record<HdesApi.EntityId, WrenchComposerApi.PageUpdate>;
    cache?: SiteCache;
    debug?: WrenchComposerApi.DebugSessions;
    commitlogs?: HdesApi.CommitLog[];
  }) {
    this._site = props.site ? props.site : { name: "", contentType: "OK", tags: {}, flows: {}, decisions: {}, services: {}, branches: {} };
    this._pages = props.pages ? props.pages : {};
    this._cache = props.cache ? props.cache : new SiteCache(this._site);
    this._debug = props.debug ? props.debug : { values: {}};
    this._commitlogs = (props.commitlogs ?? []).reduce<Record<string, HdesApi.CommitLog[]>>((collector, item) => {
      if (!collector[item.objectId]) {
        collector[item.objectId] = [];
      }
      collector[item.objectId].push(item);
      return collector;
    }, {});
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
  getDecision(decisionName: string): undefined | HdesApi.Entity<HdesApi.AstDecision> {
    return this._cache.getDecision(decisionName);
  }
  getFlow(flowName: string): undefined | HdesApi.Entity<HdesApi.AstFlow> {
    return this._cache.getFlow(flowName);
  }
  getService(serviceName: string): undefined | HdesApi.Entity<HdesApi.AstService> {
    return this._cache.getService(serviceName);
  }
  getEntity(entityId: HdesApi.EntityId): HdesApi.Entity<any> | undefined {
    return this._cache.getEntity(entityId);
  }
  getLastUpdated(entityId: HdesApi.EntityId): string | null {
    try {
      const lastUpdated = this._commitlogs[entityId];
      return lastUpdated?.[0]?.updatedAt ?? lastUpdated?.[0]?.createdAt ?? null;
    } catch (e) {
      console.error(e);
      return null;
    }
  }
  withSite(site: HdesApi.Site) {
    return new SessionData({ site: site, pages: this._pages, debug: this._debug, commitlogs: Object.values(this._commitlogs).flatMap(c => c) });
  }
  withCommitlogs(commitlogs: HdesApi.CommitLog[]) {
    return new SessionData({ site: this._site, pages: this._pages, cache: this._cache, debug: this._debug, commitlogs });
  }
  withDebug(debugSession: WrenchComposerApi.DebugSession): SessionData {
    const newDebug: Record<HdesApi.EntityId, WrenchComposerApi.DebugSession> = {};
    newDebug[debugSession.selected] = Object.assign({}, debugSession);
    const debug: WrenchComposerApi.DebugSessions = {
      selected: debugSession.selected,
      values: Object.assign({}, this._debug.values, newDebug)
    }
    return new SessionData({ site: this._site, pages: this._pages, cache: this._cache, debug, commitlogs: Object.values(this._commitlogs).flatMap(c => c) });
  }
  withBranch(branchName: string): SessionData {
    return new SessionData({ site: this._site, pages: this._pages, cache: this._cache, debug: this._debug, commitlogs: Object.values(this._commitlogs).flatMap(c => c) });
  }
  withoutPages(pageIds: HdesApi.EntityId[]): SessionData {
    const pages: Record<string, WrenchComposerApi.PageUpdate> = {};
    for (const page of Object.values(this._pages)) {
      if (pageIds.includes(page.origin.id)) {
        continue;
      }
      pages[page.origin.id] = page;
    }
    return new SessionData({ site: this._site, pages, cache: this._cache, debug: this._debug, commitlogs: Object.values(this._commitlogs).flatMap(c => c) });
  }
  withPage(page: HdesApi.EntityId): SessionData {
    if (this._pages[page]) {
      return this;
    }
    const pages = Object.assign({}, this._pages);
    const origin = this._cache.getEntity(page);
    if (!origin) {
      throw new Error("Can't find entity with id: '" + page + "'")
    }
    pages[page] = new ImmutablePageUpdate({ origin, saved: true, value: [] });
    return new SessionData({ site: this._site, pages, cache: this._cache, debug: this._debug, commitlogs: Object.values(this._commitlogs).flatMap(c => c) });
  }
  withPageValue(page: HdesApi.EntityId, value: HdesApi.AstCommand[] | string): SessionData {
    const session = this.withPage(page);
    const pageUpdate = session.pages[page];

    const pages = Object.assign({}, session.pages);
    pages[page] = pageUpdate.withValue(value);

    return new SessionData({ 
      site: session.site, 
      pages, 
      cache: this._cache, 
      debug: this._debug, 
      commitlogs: Object.values(this._commitlogs).flatMap(c => c) 
    });
  }
}

class ImmutablePageUpdate implements WrenchComposerApi.PageUpdate {
  private _saved: boolean;
  private _origin: HdesApi.Entity<any>;
  private _value: HdesApi.AstCommand[] | string;

  constructor(props: {
    saved: boolean;
    origin: HdesApi.Entity<any>;
    value: HdesApi.AstCommand[] | string;
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
  withValue(value: HdesApi.AstCommand[] | string): WrenchComposerApi.PageUpdate {
    return new ImmutablePageUpdate({ saved: false, origin: this._origin, value });
  }
}
