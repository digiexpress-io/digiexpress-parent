import { Dispatch, SetStateAction } from 'react';
import { DocumentId, Document, DocumentUpdate, Session, PageUpdate, TabBody, TabEntity, Actions } from './composer-ctx-types';

class SessionData implements Session {
  private _pages: Record<DocumentId, PageUpdate>;

  constructor(props: {
    pages?: Record<DocumentId, PageUpdate>;
  }) {
    this._pages = props.pages ? props.pages : {};
  }
  get pages() {
    return this._pages;
  }
  getEntity(entityId: DocumentId): Document | undefined {
    return {
      id: entityId,
      kind: 'HEAD'
    };
  }
  withoutPages(pageIds: DocumentId[]): SessionData {
    const pages: Record<DocumentId, PageUpdate> = {};
    for (const page of Object.values(this._pages)) {
      if (pageIds.includes(page.origin.id)) {
        continue;
      }
      pages[page.origin.id] = page;
    }
    return new SessionData({ pages });
  }
  withPage(page: DocumentId): SessionData {
    if (this._pages[page]) {
      return this;
    }
    const pages = Object.assign({}, this._pages);
    const origin = this.getEntity(page);


    if (!origin) {
      throw new Error("Can't find entity with id: '" + page + "'")
    }

    pages[page] = new ImmutablePageUpdate({ origin, saved: true, value: [] });
    return new SessionData({ pages });
  }
  withPageValue(page: DocumentId, value: DocumentUpdate[]): SessionData {
    const session = this.withPage(page);
    const pageUpdate = session.pages[page];

    const pages = Object.assign({}, session.pages);
    pages[page] = pageUpdate.withValue(value);

    return new SessionData({ pages });
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

  constructor(session: Dispatch<SetStateAction<SessionData>>) {
    this._sessionDispatch = session;
  }
  handlePageUpdate(page: DocumentId, value: DocumentUpdate[]): void {
    this._sessionDispatch((old) => old.withPageValue(page, value));
  }
  handlePageUpdateRemove(pages: DocumentId[]): void {
    this._sessionDispatch((old) => old.withoutPages(pages));
  }
}


const initSession = new SessionData({} as any);
export { SessionData, ImmutableTabData, initSession, ActionsImpl };
