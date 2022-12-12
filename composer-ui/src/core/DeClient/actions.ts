import { Dispatch, SetStateAction } from 'react';
import { DocumentId, DocumentUpdate, Actions } from './composer-types';
import { HeadState, Client } from './client-types';
import { SessionData } from './session';


class ActionsImpl implements Actions {

  private _sessionDispatch: Dispatch<SetStateAction<SessionData>>;
  private _service: Client;

  constructor(session: Dispatch<SetStateAction<SessionData>>, service: Client) {
    this._sessionDispatch = session;
    this._service = service;
  }
  async handleLoad(): Promise<void> {
    const site = await this._service.head();
    if (site.contentType === "NOT_CREATED") {
      this._service.create().head().then(created => this._sessionDispatch((old) => old.withHead(created)));
    } else {
      this._sessionDispatch((old) => old.withHead(site))
    }
  }
  async handleLoadHead(site?: HeadState): Promise<void> {
    if (site) {
      return this._sessionDispatch((old) => old.withHead(site));
    }
    const head = await this._service.head();
    this._sessionDispatch((old) => old.withHead(head));
  }
  handlePageUpdate(page: DocumentId, value: DocumentUpdate[]): void {
    this._sessionDispatch((old) => old.withPageValue(page, value));
  }
  handlePageUpdateRemove(pages: DocumentId[]): void {
    this._sessionDispatch((old) => old.withoutPages(pages));
  }
}

export default ActionsImpl;