import createMock from './mock';
import createService from './impl';


declare namespace StencilClient {

  type PageId = string;
  type LinkId = string;
  type ArticleId = string;
  type WorkflowId = string;
  type LocaleId = string;
  type Locale = string;
  type LocalisedMarkdown = string;
  type LocalisedContent = string;
  type ReleaseId = string;
  type TemplateId = string;
  type LinkType = "internal" | "external" | "phone";
  type TemplateType = "page";


  interface Site {
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

  interface SiteLocale {
    id: LocaleId,
    body: {
      value: Locale,
      enabled: boolean
    }
  }

  interface LocaleMutator {
    localeId: LocaleId,
    value: string,
    enabled: boolean
  }

  interface Page {
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

  interface PageMutator {
    pageId: PageId,
    locale: Locale;
    content: LocalisedContent;
    devMode: boolean | undefined;
  }

  interface Template {
    id: TemplateId,
    body: {
      type: TemplateType,
      name: string,
      description: string
      content: string,
    }
  }

  interface TemplateMutator {
    id: TemplateId,
    type: TemplateType,
    name: string,
    description: string,
    content: string,
  }

  interface Article {
    id: ArticleId,
    body: {
      parentId?: ArticleId,
      name: string,
      order: number,
      devMode?: boolean,
    }
  }

  interface ArticleMutator {
    articleId: ArticleId,
    parentId?: ArticleId,
    name: string,
    order: number,
    links: LinkId[] | undefined,
    workflows: WorkflowId[] | undefined,
    devMode: boolean | undefined,
  }

  interface Release {
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

  interface LinkReleaseItem extends ReleaseItem {
    value: string;
    contentType: string;
    articles: string;
    labels: LocaleLabel[];
  }
  interface WorkflowReleaseItem extends ReleaseItem {
    value: string; // pointer to actual workflow
    articles: string[];
    labels: LocaleLabel[];
  }
  interface LocaleReleaseItem extends ReleaseItem {
    value: string; // language code
  }
  interface ArticleReleaseItem extends ReleaseItem {
    name: string;
    parentId?: string;
  }
  interface PageReleaseItem extends ReleaseItem {
    locale: string;
    h1: string;
  }
  interface ReleaseItem {
    id: string;
    hash: string;
  }


  interface Link {
    id: LinkId,
    body: {
      articles: ArticleId[],
      contentType: LinkType,
      value: string, //url, phone number
      labels: LocaleLabel[],
      devMode?: boolean,
    }
  }

  interface LocaleLabel {
    locale: LocaleId;     // locale id
    labelValue: LocalisedContent; // translation in locale
  }


  interface LinkMutator {
    linkId: LinkId,
    type: LinkType,
    value: string,
    articles: ArticleId[] | undefined,
    labels: LocaleLabel[] | undefined,
    devMode: boolean | undefined
  }

  interface Workflow {
    id: WorkflowId,
    body: {
      articles: ArticleId[],
      value: string,
      labels: LocaleLabel[],
      devMode?: boolean
    }
  }

  interface WorkflowMutator {
    workflowId: WorkflowId,
    value: string,
    articles: ArticleId[] | undefined,
    labels: LocaleLabel[] | undefined,
    devMode: boolean | undefined
  }

  interface FetchIntegration {
    fetch<T>(path: string, init?: RequestInit): Promise<T>;
  }

  interface Service {
    getSite(): Promise<Site>,
    getReleaseContent(releaseId: string): Promise<{}>,

    create(): CreateBuilder;
    delete(): DeleteBuilder;
    update(): UpdateBuilder;
    version(): VersionBuilder;
  }

  interface CreateArticle {
    parentId?: ArticleId;
    name: string;
    order: number;
    devMode: boolean | undefined;
  }

  interface CreateLocale {
    locale: Locale;
  }
  interface CreatePage {
    articleId: ArticleId;
    locale: LocaleId;
    content?: string;
    devMode: boolean | undefined;
  }
  interface CreateTemplate {
    type: "page" | string;
    name: string,
    description: string;
    content: string;
  }

  interface CreateLink {
    type: "internal" | "external" | string;
    value: string;
    labels: LocaleLabel[];
    articles: ArticleId[];
    devMode: boolean | undefined;
  }

  interface CreateWorkflow {
    value: string;
    labels: LocaleLabel[];
    articles: ArticleId[];
    devMode: boolean | undefined;
  }
  interface CreateRelease {
    name: string,
    note?: string,
    created: string
  }

  interface CreateBuilder {
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
  interface DeleteBuilder {
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
  interface UpdateBuilder {
    locale(article: LocaleMutator): Promise<SiteLocale>;
    article(article: ArticleMutator): Promise<Article>;
    pages(pages: PageMutator[]): Promise<Page[]>;
    link(link: LinkMutator): Promise<Link>;
    workflow(workflow: WorkflowMutator): Promise<Workflow>;
    template(template: TemplateMutator): Promise<Template>;
  }
  interface VersionBuilder {
    version(): Promise<VersionEntity>;
  }
  interface VersionEntity {
    version: string;
    built: string;
  }
  interface Store {
    fetch<T>(path: string, init?: RequestInit): Promise<T>;
  }
  interface StoreConfig {
    url: string;
    oidc?: string;
    status?: string;
    csrf?: { key: string, value: string }
  }
  interface ErrorMsg {
    id: string;
    value: string;
  }

  interface ErrorProps {
    text: string;
    status: number;
    errors: any[];
  }
}

namespace StencilClient {
  export const mock = (): Service => {
    return createMock();
  };
  export const service = (init: { store?: Store, config?: StoreConfig }): Service => {
    return createService(init);
  };
}


export default StencilClient;



