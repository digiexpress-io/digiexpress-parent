import Burger from '@the-wrench-io/react-burger';

import type { Task } from './task-types';
import type { Profile } from './profile-types';

export type DocumentId = string; 

export type Document = 
 { kind: 'HEAD', id: string, delegate: Profile } | 
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
  profile: Profile,
  pages: Record<DocumentId, PageUpdate>;

  getEntity(id: DocumentId): undefined | Document;

  withPage(page: DocumentId): Session;
  withPageValue(page: DocumentId, value: DocumentUpdate[]): Session;
  withoutPages(pages: DocumentId[]): Session;

  withProfile(site: Profile): Session;
}

export interface Actions {
  handleLoad(): Promise<void>;
  handleLoadProfile(site?: Profile): Promise<void>;
  handlePageUpdate(page: DocumentId, value: DocumentUpdate[]): void;
  handlePageUpdateRemove(pages: DocumentId[]): void;
}




