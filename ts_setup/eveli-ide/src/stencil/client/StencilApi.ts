
export declare namespace StencilApi {

  export type PageId = string;
  export type LinkId = string;
  export type ArticleId = string;
  export type WorkflowId = string;
  export type LocaleId = string;
  export type Locale = string;
  export type LocalisedMarkdown = string;
  export type LocalisedContent = string;
  export type ReleaseId = string;
  export type TemplateId = string;
  export type LinkType = "internal" | "external" | "phone";
  export type TemplateType = "page";


  export interface Site {
    name: string,
    contentType: "OK" | "NOT_CREATED" | "EMPTY" | "ERRORS" | "NO_CONNECTION",
    locales: Record<string, SiteLocale>,
    pages: Record<PageId, Page>,
    links: Record<LinkId, Link>,
    articles: Record<ArticleId, Article>,
    workflows: Record<WorkflowId, Workflow>,
    releases: Record<ReleaseId, Release>,
    templates: Record<TemplateId, Template>
  }

  export interface SiteLocale {
    id: LocaleId,
    body: {
      value: Locale,
      enabled: boolean
    }
  }

  export interface LocaleMutator {
    localeId: LocaleId,
    value: string,
    enabled: boolean
  }

  export interface Page {
    id: PageId,
    created: string,
    modified: string,
    body: {
      article: ArticleId,
      locale: Locale,
      content: LocalisedMarkdown,
      devMode?: boolean
    }
  }

  export interface PageMutator {
    pageId: PageId,
    locale: Locale;
    content: LocalisedContent;
    devMode: boolean | undefined;
  }

  export interface Template {
    id: TemplateId,
    body: {
      type: TemplateType,
      name: string,
      description: string
      content: string,
    }
  }

  export interface TemplateMutator {
    id: TemplateId,
    type: TemplateType,
    name: string,
    description: string,
    content: string,
  }

  export interface Article {
    id: ArticleId,
    body: {
      parentId?: ArticleId,
      name: string,
      order: number,
      devMode?: boolean,
    }
  }

  export interface ArticleMutator {
    articleId: ArticleId,
    parentId?: ArticleId,
    name: string,
    order: number,
    links: LinkId[] | undefined,
    workflows: WorkflowId[] | undefined,
    devMode: boolean | undefined,
  }

  export interface Release {
    id: string,
    body: {
      note?: string,
      name: string,
      created: string,
      locales: LocaleReleaseItem[];
      articles: ArticleReleaseItem[];
      links: LinkReleaseItem[];
      workflows: WorkflowReleaseItem[];
      pages: PageReleaseItem[];
    }
  }

  export interface LinkReleaseItem extends ReleaseItem {
    value: string;
    contentType: string;
    articles: string;
    labels: LocaleLabel[];
  }
  export interface WorkflowReleaseItem extends ReleaseItem {
    value: string; // pointer to actual workflow
    articles: string[];
    labels: LocaleLabel[];
  }
  export interface LocaleReleaseItem extends ReleaseItem {
    value: string; // language code
  }
  export interface ArticleReleaseItem extends ReleaseItem {
    name: string;
    parentId?: string;
  }
  export interface PageReleaseItem extends ReleaseItem {
    locale: string;
    h1: string;
  }
  export interface ReleaseItem {
    id: string;
    hash: string;
  }


  export interface Link {
    id: LinkId,
    body: {
      articles: ArticleId[],
      contentType: LinkType,
      value: string, //url, phone number
      labels: LocaleLabel[],
      devMode?: boolean,
    }
  }

  export interface LocaleLabel {
    locale: LocaleId;     // locale id
    labelValue: LocalisedContent; // translation in locale
  }


  export interface LinkMutator {
    linkId: LinkId,
    type: LinkType,
    value: string,
    articles: ArticleId[] | undefined,
    labels: LocaleLabel[] | undefined,
    devMode: boolean | undefined
  }

  export interface Workflow {
    id: WorkflowId,
    body: {
      articles: ArticleId[],
      value: string,
      labels: LocaleLabel[],
      devMode?: boolean,
      startDate?: string | undefined,
      endDate?: string | undefined,
    }
  }

  export interface WorkflowMutator {
    workflowId: WorkflowId,
    value: string,
    articles: ArticleId[] | undefined,
    labels: LocaleLabel[] | undefined,
    devMode: boolean | undefined,
    startDate?: string | undefined,
    endDate?: string | undefined,
  }

  export interface FetchIntegration {
    fetch<T>(path: string, init?: RequestInit): Promise<T>;
  }

  export interface Service {
    getSite(): Promise<Site>,
    getReleaseContent(releaseId: string): Promise<{}>,

    create(): CreateBuilder;
    delete(): DeleteBuilder;
    update(): UpdateBuilder;
    version(): Promise<VersionEntity>;
  }

  export interface VersionEntity {
    version: string;
    built: string;
  }

  export interface CreateArticle {
    parentId?: ArticleId;
    name: string;
    order: number;
    devMode: boolean | undefined;
  }

  export interface CreateLocale {
    locale: Locale;
  }
  export interface CreatePage {
    articleId: ArticleId;
    locale: LocaleId;
    content?: string;
    devMode: boolean | undefined;
  }
  export interface CreateTemplate {
    type: "page" | string;
    name: string,
    description: string;
    content: string;
  }

  export interface CreateLink {
    type: "internal" | "external" | string;
    value: string;
    labels: LocaleLabel[];
    articles: ArticleId[];
    devMode: boolean | undefined;
  }

  export interface CreateWorkflow {
    value: string;
    labels: LocaleLabel[];
    articles: ArticleId[];
    devMode: boolean | undefined;
    startDate?: string | undefined;
    endDate?: string | undefined;
  }
  export interface CreateRelease {
    name: string,
    note?: string,
    created: string
  }

  export interface CreateBuilder {
    site(): Promise<Site>;
    importData(init: string): Promise<void>;
    release(init: CreateRelease): Promise<Release>;
    locale(init: CreateLocale): Promise<SiteLocale>;
    article(init: CreateArticle): Promise<Article>;
    page(init: CreatePage): Promise<Page>;
    link(init: CreateLink): Promise<Link>;
    template(init: CreateTemplate): Promise<Template>;
    workflow(init: CreateWorkflow): Promise<Workflow>;
  }
  export interface DeleteBuilder {
    locale(id: LocaleId): Promise<void>;
    article(id: ArticleId): Promise<void>;
    page(id: PageId): Promise<void>;
    link(id: LinkId): Promise<void>;
    template(id: TemplateId): Promise<void>;
    linkArticlePage(link: LinkId, article: ArticleId, locale: Locale): Promise<void>;
    workflow(id: WorkflowId): Promise<void>;
    workflowArticlePage(workflow: WorkflowId, article: ArticleId, locale: Locale): Promise<void>;
    release(id: ReleaseId): Promise<void>;

  }
  export interface UpdateBuilder {
    locale(article: LocaleMutator): Promise<SiteLocale>;
    article(article: ArticleMutator): Promise<Article>;
    pages(pages: PageMutator[]): Promise<Page[]>;
    link(link: LinkMutator): Promise<Link>;
    workflow(workflow: WorkflowMutator): Promise<Workflow>;
    template(template: TemplateMutator): Promise<Template>;
  }

  export interface Store {
    fetch<T>(path: string, init?: RequestInit): Promise<T>;
  }
  export interface StoreConfig {
    url: string;
    oidc?: string;
    status?: string;
    csrf?: { key: string, value: string }
  }
  export interface ErrorMsg {
    id: string;
    value: string;
  }

  export interface ErrorProps {
    text: string;
    status: number;
    errors: any[];
  }
}





