import { Dispatch, SetStateAction } from 'react';
import { DocumentId, Document, DocumentUpdate, Session, PageUpdate, TabBody, TabEntity, Actions } from './composer-ctx-types';

import type { Backend } from 'client';
import { UserProfileAndOrg } from 'descriptor-user-profile';

class SiteCache {
  private _site: UserProfileAndOrg;
  constructor(site: UserProfileAndOrg) {
    this._site = site;
  }

  getEntity(entityId: DocumentId): Document {

    return {
      id: entityId,
      delegate: this._site,
      kind: 'HEAD'
    };
  }
}

class SessionData implements Session {
  private _profile: UserProfileAndOrg;
  private _pages: Record<DocumentId, PageUpdate>;
  private _cache: SiteCache;

  constructor(props: {
    profile?: UserProfileAndOrg;
    pages?: Record<DocumentId, PageUpdate>;
    cache?: SiteCache;
  }) {
    this._profile = props.profile ? props.profile : {
      user: {
        id: '',
        created: new Date().toISOString(),
        updated: new Date().toISOString(),
        details: {
          firstName: '',
          lastName: '',
          username: '',
          email: ''
        },
        notificationSettings: [{
          type: '',
          enabled: true
        }]
      },
      today: new Date(), userId: "", roles: []
    };
    this._pages = props.pages ? props.pages : {};
    this._cache = props.cache ? props.cache : new SiteCache(this._profile);
  }
  get profile() {
    return this._profile;
  }
  get pages() {
    return this._pages;
  }
  getEntity(entityId: DocumentId): Document | undefined {
    return this._cache.getEntity(entityId);
  }
  withProfile(profile: UserProfileAndOrg) {
    if (!profile) {
      console.error("Head not defined error", profile);
      return this;
    }
    return new SessionData({ profile, pages: this._pages });
  }
  withoutPages(pageIds: DocumentId[]): SessionData {
    const pages: Record<DocumentId, PageUpdate> = {};
    for (const page of Object.values(this._pages)) {
      if (pageIds.includes(page.origin.id)) {
        continue;
      }
      pages[page.origin.id] = page;
    }
    return new SessionData({ profile: this._profile, pages, cache: this._cache });
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
    return new SessionData({ profile: this._profile, pages, cache: this._cache });
  }
  withPageValue(page: DocumentId, value: DocumentUpdate[]): SessionData {
    const session = this.withPage(page);
    const pageUpdate = session.pages[page];

    const pages = Object.assign({}, session.pages);
    pages[page] = pageUpdate.withValue(value);

    return new SessionData({ profile: session.profile, pages, cache: this._cache });
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


class ActionsImpl implements Actions {

  private _sessionDispatch: Dispatch<SetStateAction<SessionData>>;
  private _service: Backend;

  constructor(session: Dispatch<SetStateAction<SessionData>>, service: Backend) {
    this._sessionDispatch = session;
    this._service = service;
  }
  async handleLoad(): Promise<void> {
    const site = await this._service.currentUserProfile();
    this._sessionDispatch((old) => old.withProfile(site))
  }
  async handleLoadProfile(site?: UserProfileAndOrg): Promise<void> {
    if (site) {
      return this._sessionDispatch((old) => old.withProfile(site));
    }
    const head = await this._service.currentUserProfile();
    this._sessionDispatch((old) => old.withProfile(head));
  }
  handlePageUpdate(page: DocumentId, value: DocumentUpdate[]): void {
    this._sessionDispatch((old) => old.withPageValue(page, value));
  }
  handlePageUpdateRemove(pages: DocumentId[]): void {
    this._sessionDispatch((old) => old.withoutPages(pages));
  }
}


const initSession = new SessionData({});
export { SessionData, ImmutableTabData, initSession, ActionsImpl };
