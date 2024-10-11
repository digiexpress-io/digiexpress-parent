import StencilComposerApi from './ide';
import { StencilApi } from '../client';

class ImmutableSearchData implements StencilComposerApi.SearchData {
  private _articles: ImmutableArticleSearchEntry[];
  private _links: ImmutableLinkSearchEntry[];
  private _workflows: ImmutableWorkflowSearchEntry[];

  constructor(
    articles: ImmutableArticleSearchEntry[],
    links: ImmutableLinkSearchEntry[],
    workflows: ImmutableWorkflowSearchEntry[]) {

    this._articles = articles;
    this._links = links;
    this._workflows = workflows;
  }

  get values(): StencilComposerApi.SearchDataEntry[] {
    return [...this._articles, ...this._links, ...this._workflows];
  }

  filterArticles(keyword: string): StencilComposerApi.SearchResult[] {
    const results: StencilComposerApi.SearchResult[] = [];
    const keywordLowerCase = keyword.toLowerCase();
    for (const article of this._articles) {
      const match = this.findMatch(article, keywordLowerCase);
      if (match) {
        results.push(match);
      }
    }
    return results;
  }
  filterWorkflows(keyword: string): StencilComposerApi.SearchResult[] {
    const results: StencilComposerApi.SearchResult[] = [];
    const keywordLowerCase = keyword.toLowerCase();
    for (const workflow of this._workflows) {
      const match = this.findMatch(workflow, keywordLowerCase);
      if (match) {
        results.push(match);
      }
    }
    return results;
  }
  filterLinks(keyword: string): StencilComposerApi.SearchResult[] {
    const results: StencilComposerApi.SearchResult[] = [];
    const keywordLowerCase = keyword.toLowerCase();
    for (const link of this._links) {
      const match = this.findMatch(link, keywordLowerCase);
      if (match) {
        results.push(match);
      }
    }
    return results;
  }

  findMatch(source: StencilComposerApi.SearchDataEntry, keyword: string): StencilComposerApi.SearchResult | undefined {

    let matches: StencilComposerApi.SearchableValue[] = []
    for (const searchableValue of source.values) {
      if (searchableValue.value.toLowerCase().indexOf(keyword) > -1) {
        matches.push(searchableValue);
      }
    }

    return matches.length > 0 ? { source, matches } : undefined;

  }
}

class SiteCache {
  private _articleSearchData: Record<StencilApi.ArticleId, ImmutableArticleSearchEntry> = {};
  private _linkSearchData: Record<StencilApi.ArticleId, ImmutableLinkSearchEntry> = {};
  private _workflowSearchData: Record<StencilApi.ArticleId, ImmutableWorkflowSearchEntry> = {};
  private _searchData: StencilComposerApi.SearchData;

  private _site: StencilApi.Site;
  private _articles: Record<StencilApi.ArticleId, StencilComposerApi.ArticleView> = {};
  private _workflows: Record<StencilApi.WorkflowId, StencilComposerApi.WorkflowView> = {};
  private _links: Record<StencilApi.LinkId, StencilComposerApi.LinkView> = {};

  private _pagesByArticle: Record<StencilApi.ArticleId, StencilComposerApi.PageView[]> = {};
  private _linksByArticle: Record<StencilApi.ArticleId, StencilComposerApi.LinkView[]> = {};
  private _workflowsByArticle: Record<StencilApi.ArticleId, StencilComposerApi.WorkflowView[]> = {};

  constructor(site: StencilApi.Site) {
    this._site = site;
    Object.values(site.pages).sort((l0, l1) => l0.body.locale.localeCompare(l1.body.locale)).forEach(page => this.visitPage(page))
    Object.values(site.links).sort((l0, l1) => l0.body.contentType.localeCompare(l1.body.contentType)).forEach(link => this.visitLink(link))
    Object.values(site.workflows).sort((l0, l1) => l0.body.value.localeCompare(l1.body.value)).forEach(workflow => this.visitWorkflow(workflow))

    Object.values(site.articles).sort((a1, a2) => {
      if (a1.body.parentId && a1.body.parentId === a2.body.parentId) {
        const children = a1.body.order - a2.body.order;
        if (children === 0) {
          return a1.body.name.localeCompare(a2.body.name);
        }
        return children;
      }

      return (a1.body.parentId ? site.articles[a1.body.parentId].body.order + 1 : a1.body.order)
        - (a2.body.parentId ? site.articles[a2.body.parentId].body.order + 1 : a2.body.order);

    }).forEach(article => this.visitArticle(article));

    this._searchData = new ImmutableSearchData(
      Object.values(this._articleSearchData),
      Object.values(this._linkSearchData),
      Object.values(this._workflowSearchData)
    );
  }

  getSearchData() {
    return this._searchData;
  }
  getArticles() {
    return this._articles;
  }
  getWorkflows() {
    return this._workflows;
  }
  getLinks() {
    return this._links;
  }
  private visitPage(page: StencilApi.Page) {
    const site = this._site;
    const view = new ImmutablePageView({ page, locale: site.locales[page.body.locale] });
    const articleId = page.body.article;
    let articlePages = this._pagesByArticle[articleId];
    if (!articlePages) {
      articlePages = [];
      this._pagesByArticle[articleId] = articlePages;
    }
    articlePages.push(view);

    // article search data
    let searchData = this._articleSearchData[articleId];
    if (!searchData) {
      searchData = new ImmutableArticleSearchEntry({ id: articleId, values: [] })
    }
    this._articleSearchData[articleId] = searchData.withPage(view);
  }

  private visitLink(link: StencilApi.Link) {
    const site = this._site;
    const view = new ImmutableLinkView({
      link,
      labels: link.body.labels.map(l => (new ImmutableLabelView({ label: l, locale: site.locales[l.locale] })))
    });

    this._links[view.link.id] = view;
    for (const articleId of link.body.articles) {
      let articleLinks = this._linksByArticle[articleId];
      if (!articleLinks) {
        articleLinks = [];
        this._linksByArticle[articleId] = articleLinks;
      }
      articleLinks.push(view);
    }

    // link search data
    let searchData = this._linkSearchData[link.id];
    if (!searchData) {
      searchData = new ImmutableLinkSearchEntry({ id: link.id, values: [] })
    }
    this._linkSearchData[link.id] = searchData.withLink(view);
  }
  private visitWorkflow(workflow: StencilApi.Workflow) {
    const site = this._site;
    const view = new ImmutableWorkflowView({
      workflow,
      labels: workflow.body.labels.map(l => (new ImmutableLabelView({ label: l, locale: site.locales[l.locale] })))
    });

    this._workflows[view.workflow.id] = view;
    for (const articleId of workflow.body.articles) {
      let articleWorkflows = this._workflowsByArticle[articleId];
      if (!articleWorkflows) {
        articleWorkflows = [];
        this._workflowsByArticle[articleId] = articleWorkflows;
      }
      articleWorkflows.push(view);
    }

    // link search data
    let searchData = this._workflowSearchData[workflow.id];
    if (!searchData) {
      searchData = new ImmutableWorkflowSearchEntry({ id: workflow.id, values: [] })
    }
    this._workflowSearchData[workflow.id] = searchData.withWorkflow(view);
  }
  private visitArticle(article: StencilApi.Article) {
    const articleId = article.id;
    const site = this._site;
    const pages: StencilComposerApi.PageView[] = Object.values(site.pages)
      .filter(page => articleId === page.body.article)
      .map(page => new ImmutablePageView({ page, locale: site.locales[page.body.locale] }));

    const links: StencilComposerApi.LinkView[] = this.empty(this._linksByArticle[articleId]);
    const workflows: StencilComposerApi.WorkflowView[] = this.empty(this._workflowsByArticle[articleId]);

    const canCreate: StencilApi.SiteLocale[] = Object.values(site.locales).filter(locale => pages.filter(p => p.page.body.locale === locale.id).length === 0);
    const view = new ImmutableArticleView({
      article, pages, canCreate,
      links,
      workflows,
      children: [],
      displayOrder: 10000 + article.body.order + (article.body.parentId ? this._site.articles[article.body.parentId].body.order : 0)
    });

    if (article.body.parentId) {
      const parent = this._articles[article.body.parentId];
      if (parent) {
        this._articles[parent.article.id] = new ImmutableArticleView({
          article: parent.article,
          pages: parent.pages,
          canCreate: parent.canCreate,
          links: parent.links,
          workflows: parent.workflows,
          children: [...parent.children, view],
          displayOrder: 10000 + article.body.order + (article.body.parentId ? this._site.articles[article.body.parentId].body.order : 0)
        });
      } else {
        console.error("Failed to attach to parent");
      }
    }

    this._articles[articleId] = view;

    // search data
    let searchData = this._articleSearchData[articleId];
    if (!searchData) {
      searchData = new ImmutableArticleSearchEntry({ id: articleId, values: [] })
    }
    this._articleSearchData[articleId] = searchData.withArticle(view);
  }

  private empty<T>(arrayType: T) {
    return arrayType ? arrayType : [];
  }
}


class ImmutableSessionFilter implements StencilComposerApi.SessionFilter {
  private _locale?: string;

  constructor(props: {
    locale?: string
  }) {
    this._locale = props.locale;
  }

  get locale() {
    return this._locale;
  }
  withLocale(locale?: StencilApi.LocaleId) {
    return new ImmutableSessionFilter({ locale });
  }
}

class SessionData implements StencilComposerApi.Session {
  private _site: StencilApi.Site;
  private _pages: Record<StencilApi.PageId, StencilComposerApi.PageUpdate>;
  private _cache: SiteCache;
  private _filter: StencilComposerApi.SessionFilter;

  constructor(props: {
    site?: StencilApi.Site,
    pages?: Record<StencilApi.PageId, StencilComposerApi.PageUpdate>,
    cache?: SiteCache;
    filter?: StencilComposerApi.SessionFilter;
  }) {
    this._filter = props.filter ? props.filter : new ImmutableSessionFilter({});
    this._site = props.site ? props.site : { name: "", contentType: "OK", releases: {}, articles: {}, links: {}, locales: {}, pages: {}, workflows: {}, templates: {} };
    this._pages = props.pages ? props.pages : {};
    this._cache = props.cache ? props.cache : new SiteCache(this._site);
  }
  get search() {
    return this._cache.getSearchData();
  }
  get filter() {
    return this._filter;
  }
  get articles() {
    return Object.values(this._cache.getArticles());
  }
  get workflows() {
    return Object.values(this._cache.getWorkflows());
  }
  get links() {
    return Object.values(this._cache.getLinks());
  }
  get site() {
    return this._site;
  }
  get pages() {
    return this._pages;
  }
  getArticleName(articleId: StencilApi.ArticleId) {
    return this.getArticleNameInternal(articleId, []);
  }
  getArticleNameInternal(articleId: StencilApi.ArticleId, visited: StencilApi.ArticleId[]): { missing: boolean, name: string } {
    if(visited.includes(articleId)) {
      return { missing: true, name: 'parent-child-cycle-for-article-' + articleId };
    }
    visited.push(articleId);
    const article = this.getArticleView(articleId);
    const articleName = article.article.body.name;
    const locale = this._filter.locale;

    const parent = article.article.body.parentId ? this.getArticleNameInternal(article.article.body.parentId, visited).name + "/" : ""

    if (locale) {
      const pages = article.pages.filter(p => p.locale?.id === locale);
      if (pages.length === 0) {
        return { missing: true, name: "_not_translated_" + articleName };
      }
      const name = pages.length ? pages[0].title : '';
      return { missing: false, name: name ? parent + name : parent + 'no-h1' };
    }

    return { missing: false, name: parent + articleName };
  }
  getWorkflowName(workflowId: StencilApi.WorkflowId) {
    const view = this.getWorkflowView(workflowId);
    const workflowName: string = view.workflow.body.value;
    const locale = this._filter.locale;

    if (locale) {
      const pages = view.labels.filter(p => p.locale?.id === locale);
      if (pages.length === 0) {
        return { missing: true, name: "_not_translated_" + workflowName };
      }
      const name = pages.length ? pages[0].label.labelValue : '';
      return { missing: false, name: name ? name : 'no-h1' };
    }
    return { missing: false, name: workflowName };
  }
  getLinkName(workflowId: StencilApi.WorkflowId) {
    const view = this.getLinkView(workflowId);
    const linkName = view.link.body.value;
    const locale = this._filter.locale;

    if (locale) {
      const pages = view.labels.filter(p => p.locale.id === locale);
      if (pages.length === 0) {
        return { missing: true, name: "_not_translated_" + linkName };
      }
      const name = pages.length ? pages[0].label.labelValue : '';
      return { missing: false, name: name ? name : 'no-h1' };
    }
    return { missing: false, name: linkName };
  }
  getArticleView(articleId: StencilApi.ArticleId): StencilComposerApi.ArticleView {
    return this._cache.getArticles()[articleId];
  }
  getWorkflowView(workflowId: StencilApi.WorkflowId): StencilComposerApi.WorkflowView {
    return this._cache.getWorkflows()[workflowId];
  }
  getLinkView(linkId: StencilApi.LinkId): StencilComposerApi.LinkView {
    return this._cache.getLinks()[linkId];
  }

  getArticlesForLocale(locale: StencilApi.LocaleId): StencilApi.Article[] {
    const pages = Object.values(this._site.pages)
    return locale ? Object.values(this._site.articles).filter(article => {
      for (const page of pages) {
        if (page.body.article === article.id && page.body.locale === locale) {
          return true;
        }
      }
      return false;
    }) : []
  }
  getArticlesForLocales(locales: StencilApi.LocaleId[]): StencilApi.Article[] {
    const pages = Object.values(this._site.pages)
    return locales && locales.length > 0 ? Object.values(this._site.articles).filter(article => {
      for (const page of pages) {
        if (page.body.article === article.id && locales.includes(page.body.locale)) {
          return true;
        }
      }
      return false;
    }) : []
  }

  withSite(site: StencilApi.Site) {
    return new SessionData({ site: site, pages: this._pages, filter: this._filter });
  }
  withoutPages(pageIds: StencilApi.PageId[]): StencilComposerApi.Session {
    const pages: Record<string, StencilComposerApi.PageUpdate> = {};
    for (const page of Object.values(this._pages)) {
      if (pageIds.includes(page.origin.id)) {
        continue;
      }
      pages[page.origin.id] = page;
    }
    return new SessionData({ site: this._site, pages, cache: this._cache, filter: this._filter });
  }
  withPage(page: StencilApi.PageId): StencilComposerApi.Session {
    if (this._pages[page]) {
      return this;
    }
    const pages = Object.assign({}, this._pages);
    const origin = this._site.pages[page];
    pages[page] = new ImmutablePageUpdate({ origin, saved: true, value: origin.body.content });
    return new SessionData({ site: this._site, pages, cache: this._cache, filter: this._filter });
  }
  withPageValue(page: StencilApi.PageId, value: StencilApi.LocalisedContent): StencilComposerApi.Session {
    const session = this.withPage(page);
    const pageUpdate = session.pages[page];

    const pages = Object.assign({}, session.pages);
    pages[page] = pageUpdate.withValue(value);

    return new SessionData({ site: session.site, pages, cache: this._cache, filter: this._filter });
  }

  withLocaleFilter(locale?: StencilApi.LocaleId) {
    return new SessionData({ site: this._site, pages: this._pages, cache: this._cache, filter: this._filter.withLocale(locale) });
  }
}

class ImmutablePageUpdate implements StencilComposerApi.PageUpdate {
  private _saved: boolean;
  private _origin: StencilApi.Page;
  private _value: StencilApi.LocalisedContent;

  constructor(props: {
    saved: boolean;
    origin: StencilApi.Page;
    value: StencilApi.LocalisedContent;
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
  withValue(value: StencilApi.LocalisedContent): StencilComposerApi.PageUpdate {
    return new ImmutablePageUpdate({ saved: false, origin: this._origin, value });
  }
}


class ImmutableArticleView implements StencilComposerApi.ArticleView {
  private _article: StencilApi.Article;
  private _pages: StencilComposerApi.PageView[];
  private _canCreate: StencilApi.SiteLocale[];
  private _links: StencilComposerApi.LinkView[];
  private _workflows: StencilComposerApi.WorkflowView[];
  private _children: StencilComposerApi.ArticleView[];
  private _displayOrder: number;
  constructor(props: {
    article: StencilApi.Article;
    pages: StencilComposerApi.PageView[];
    canCreate: StencilApi.SiteLocale[];
    links: StencilComposerApi.LinkView[];
    workflows: StencilComposerApi.WorkflowView[];
    children: StencilComposerApi.ArticleView[];
    displayOrder: number;
  }) {
    this._article = props.article;
    this._pages = props.pages;
    this._canCreate = props.canCreate;
    this._links = props.links;
    this._workflows = props.workflows;
    this._children = props.children;
    this._displayOrder = props.displayOrder;
  }
  get displayOrder(): number { return this._displayOrder };
  get children(): StencilComposerApi.ArticleView[] { return this._children };
  get article(): StencilApi.Article { return this._article };
  get pages(): StencilComposerApi.PageView[] { return this._pages };
  get canCreate(): StencilApi.SiteLocale[] { return this._canCreate };
  get links(): StencilComposerApi.LinkView[] { return this._links };
  get workflows(): StencilComposerApi.WorkflowView[] { return this._workflows };
  getPageById(id: StencilApi.PageId): StencilComposerApi.PageView {
    const found = this._pages.find(p => p.page.id === id);
    if(!found) {
      throw new Error(`No page with page id: {id}!`);
    }
    return found;
  }
  getPageByLocaleId(id: StencilApi.LocaleId): StencilComposerApi.PageView {
    const found = this.findPageByLocaleId(id);
    if(!found) {
      throw new Error(`No page with locale id: {id}!`);
    }
    return found;
  }
  findPageByLocaleId(id: StencilApi.LocaleId): StencilComposerApi.PageView | undefined {
    return this._pages.find(p => p.page.body.locale === id);
  }
}

class ImmutablePageView implements StencilComposerApi.PageView {
  private _page: StencilApi.Page;
  private _locale: StencilApi.SiteLocale;
  private _title: string;

  constructor(props: {
    page: StencilApi.Page;
    locale: StencilApi.SiteLocale;
  }) {
    this._page = props.page;
    this._locale = props.locale;
    this._title = this.getTitle(props.page);
  }

  private getTitle(page: StencilApi.Page) {
    const heading1 = page.body.content.indexOf("# ");

    if (heading1 === -1) {
      return page.body.content.substring(0, Math.min(page.body.content.length, 30));
    }
    const lineBreak1 = page.body.content.indexOf("\n", heading1)
    if (lineBreak1 > 0) {
      return page.body.content.substring(0, Math.min(lineBreak1, 30)).substring(2);
    }

    const lineBreak2 = page.body.content.indexOf("\r\n", heading1)
    if (lineBreak2 > 0) {
      return page.body.content.substring(0, Math.min(lineBreak2, 30)).substring(2);
    }

    return page.body.content.substring(2);

  }

  get title(): string { return this._title };
  get page(): StencilApi.Page { return this._page };
  get locale(): StencilApi.SiteLocale { return this._locale };
}


class ImmutableLinkView implements StencilComposerApi.LinkView {
  private _link: StencilApi.Link;
  private _labels: StencilComposerApi.LabelView[];

  constructor(props: {
    link: StencilApi.Link;
    labels: StencilComposerApi.LabelView[];
  }) {
    this._link = props.link;
    this._labels = props.labels;
  }

  get link(): StencilApi.Link { return this._link };
  get labels(): StencilComposerApi.LabelView[] { return this._labels };
}

class ImmutableWorkflowView implements StencilComposerApi.WorkflowView {
  private _workflow: StencilApi.Workflow;
  private _labels: StencilComposerApi.LabelView[];

  constructor(props: {
    workflow: StencilApi.Workflow;
    labels: StencilComposerApi.LabelView[];
  }) {
    this._workflow = props.workflow;
    this._labels = props.labels;
  }

  get workflow(): StencilApi.Workflow { return this._workflow };
  get labels(): StencilComposerApi.LabelView[] { return this._labels };
}

class ImmutableLabelView implements StencilComposerApi.LabelView {
  private _label: StencilApi.LocaleLabel;
  private _locale: StencilApi.SiteLocale;

  constructor(props: {
    label: StencilApi.LocaleLabel;
    locale: StencilApi.SiteLocale;
  }) {
    this._label = props.label;
    this._locale = props.locale;
  }

  get locale(): StencilApi.SiteLocale { return this._locale };
  get label(): StencilApi.LocaleLabel { return this._label };
}



class ImmutableArticleSearchEntry implements StencilComposerApi.SearchDataEntry {
  private _id: string;
  private _values: StencilComposerApi.SearchableValue[];


  constructor(props: {
    id: StencilApi.ArticleId;
    values: StencilComposerApi.SearchableValue[];
  }) {
    this._id = props.id;
    this._values = props.values;
    this._values.sort((e1, e2) => e1.type.localeCompare(e2.type));
  }
  get id() { return this._id }
  get values() { return this._values }
  get type(): "ARTICLE" { return "ARTICLE" };

  withPage(view: StencilComposerApi.PageView): ImmutableArticleSearchEntry {
    const values: StencilComposerApi.SearchableValue[] = [...this.values];
    values.push({ type: "ARTICLE_PAGE", value: view.page.body.content, id: view.page.id });
    return new ImmutableArticleSearchEntry({ id: this._id, values });
  }

  withArticle(view: StencilComposerApi.ArticleView): ImmutableArticleSearchEntry {
    const values: StencilComposerApi.SearchableValue[] = [...this.values];
    values.push({ type: "ARTICLE_NAME", value: view.article.body.name, id: view.article.id });
    return new ImmutableArticleSearchEntry({ id: this._id, values });
  }
}

class ImmutableLinkSearchEntry implements StencilComposerApi.SearchDataEntry {
  private _id: string;
  private _values: StencilComposerApi.SearchableValue[];


  constructor(props: {
    id: StencilApi.LinkId;
    values: StencilComposerApi.SearchableValue[];
  }) {
    this._id = props.id;
    this._values = props.values;
  }
  get id() { return this._id }
  get values() { return this._values }
  get type(): "LINK" { return "LINK" };

  withLink(view: StencilComposerApi.LinkView): ImmutableLinkSearchEntry {
    const values: StencilComposerApi.SearchableValue[] = [...this.values];
    values.push({ type: "LINK_VALUE", value: view.link.body.value, id: view.link.id });

    for (const label of view.labels) {
      values.push({ type: "LINK_LABEL", value: label.label.labelValue, id: label.locale.id });
    }
    return new ImmutableLinkSearchEntry({ id: this._id, values });
  }
}



class ImmutableWorkflowSearchEntry implements StencilComposerApi.SearchDataEntry {
  private _id: string;
  private _values: StencilComposerApi.SearchableValue[];

  constructor(props: {
    id: StencilApi.WorkflowId;
    values: StencilComposerApi.SearchableValue[];
  }) {
    this._id = props.id;
    this._values = props.values;
  }
  get id() { return this._id }
  get values() { return this._values }
  get type(): "WORKFLOW" { return "WORKFLOW" };

  withWorkflow(view: StencilComposerApi.WorkflowView): ImmutableWorkflowSearchEntry {
    const values: StencilComposerApi.SearchableValue[] = [...this.values];
    values.push({ type: "WORKFLOW_NAME", value: view.workflow.body.value, id: view.workflow.id });

    for (const label of view.labels) {
      if (!label.locale) {
        console.error("no locale", label);
      }
      values.push({ type: "WORKFLOW_LABEL", value: label.label.labelValue, id: label.locale?.id });
    }
    return new ImmutableWorkflowSearchEntry({ id: this._id, values });
  }
}


export { SessionData, SiteCache };
