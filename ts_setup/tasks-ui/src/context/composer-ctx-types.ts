import Burger from 'components-burger';


export type DocumentId = string;
export type Document = { kind: 'HEAD', id: string };

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
  pages: Record<DocumentId, PageUpdate>;

  getEntity(id: DocumentId): undefined | Document;

  withPage(page: DocumentId): Session;
  withPageValue(page: DocumentId, value: DocumentUpdate[]): Session;
  withoutPages(pages: DocumentId[]): Session;
}

export interface Actions {
  handlePageUpdate(page: DocumentId, value: DocumentUpdate[]): void;
  handlePageUpdateRemove(pages: DocumentId[]): void;
}




