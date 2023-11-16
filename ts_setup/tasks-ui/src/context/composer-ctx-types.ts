import Burger from 'components-burger';

import type { Task, UserProfile } from 'client';

export type DocumentId = string;

export type Document =
  { kind: 'HEAD', id: string, delegate: UserProfile } |
  { kind: 'TASK', id: string, delegate: Task };

export type DocumentUpdate = {};


export interface TabEntity {
  value?: string | null;
}

export interface TabBody {
  nav?: TabEntity
  withNav(nav: TabEntity): TabBody;
}
export interface Tab extends Burger.TabSession<TabBody> { }

export interface PageUpdate {
  saved: boolean;
  origin: Document;
  value: DocumentUpdate[];
  withValue(value: DocumentUpdate): PageUpdate;
}

export interface Session {
  profile: UserProfile,
  pages: Record<DocumentId, PageUpdate>;

  getEntity(id: DocumentId): undefined | Document;

  withPage(page: DocumentId): Session;
  withPageValue(page: DocumentId, value: DocumentUpdate[]): Session;
  withoutPages(pages: DocumentId[]): Session;

  withProfile(site: UserProfile): Session;
}

export interface Actions {
  handleLoad(): Promise<void>;
  handleLoadProfile(site?: UserProfile): Promise<void>;
  handlePageUpdate(page: DocumentId, value: DocumentUpdate[]): void;
  handlePageUpdateRemove(pages: DocumentId[]): void;
}




