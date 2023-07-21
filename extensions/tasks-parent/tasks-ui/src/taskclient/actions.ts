import { Dispatch, SetStateAction } from 'react';
import { DocumentId, DocumentUpdate, Actions } from './composer-types';
import { Client } from './client-types';
import { Profile } from './profile-types';
import { SessionData } from './session';


class ActionsImpl implements Actions {

  private _sessionDispatch: Dispatch<SetStateAction<SessionData>>;
  private _service: Client;

  constructor(session: Dispatch<SetStateAction<SessionData>>, service: Client) {
    this._sessionDispatch = session;
    this._service = service;
  }
  async handleLoad(): Promise<void> {
    const site = await this._service.profile.getProfile();
    if (site.contentType === "NOT_CREATED") {
      this._service.profile.createProfile().then(created => this._sessionDispatch((old) => old.withProfile(created)));
    } else {
      this._sessionDispatch((old) => old.withProfile(site))
    }
  }
  async handleLoadProfile(site?: Profile): Promise<void> {
    if (site) {
      return this._sessionDispatch((old) => old.withProfile(site));
    }
    const head = await this._service.profile.getProfile();
    this._sessionDispatch((old) => old.withProfile(head));
  }
  handlePageUpdate(page: DocumentId, value: DocumentUpdate[]): void {
    this._sessionDispatch((old) => old.withPageValue(page, value));
  }
  handlePageUpdateRemove(pages: DocumentId[]): void {
    this._sessionDispatch((old) => old.withoutPages(pages));
  }
}

export default ActionsImpl;