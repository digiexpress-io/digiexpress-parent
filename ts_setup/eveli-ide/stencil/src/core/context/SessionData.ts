import Composer from './ide';
import StencilClient from '../client';

class ImmutableSearchData implements Composer.SearchData {
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

  get values(): Composer.SearchDataEntry[] {
    return [...this._articles, ...this._links, ...this._workflows];
  }

  filterArticles(keyword: string): Composer.SearchResult[] {
    const results: Composer.SearchResult[] = [];
    const keywordLowerCase = keyword.toLowerCase();
    for (const article of this._articles) {
      const match = this.findMatch(article, keywordLowerCase);
      if (match) {
        results.push(match);
      }
    }
    return results;
  }
  filterWorkflows(keyword: string): Composer.SearchResult[] {
    const results: Composer.SearchResult[] = [];
    const keywordLowerCase = keyword.toLowerCase();
    for (const workflow of this._workflows) {
      const match = this.findMatch(workflow, keywordLowerCase);
      if (match) {
        results.push(match);
      }
    }
    return results;
  }
  filterLinks(keyword: string): Composer.SearchResult[] {
    const results: Composer.SearchResult[] = [];
    const keywordLowerCase = keyword.toLowerCase();
    for (const link of this._links) {
      const match = this.findMatch(link, keywordLowerCase);
      if (match) {
        results.push(match);
      }
    }
    return results;
  }

  findMatch(source: Composer.SearchDataEntry, keyword: string): Composer.SearchResult | undefined {

    let matches: Composer.SearchableValue[] = []
    for (const searchableValue of source.values) {
      if (searchableValue.value.toLowerCase().indexOf(keyword) > -1) {
        matches.push(searchableValue);
      }
    }

    return matches.length > 0 ? { source, matches } : undefined;

  }
}

class SiteCache {
  private _articleSearchData: Record<StencilClient.ArticleId, ImmutableArticleSearchEntry> = {};
  private _linkSearchData: Record<StencilClient.ArticleId, ImmutableLinkSearchEntry> = {};
  private _workflowSearchData: Record<StencilClient.ArticleId, ImmutableWorkflowSearchEntry> = {};
  private _searchData: Composer.SearchData;

  private _site: StencilClient.Site;
  private _articles: Record<StencilClient.ArticleId, Composer.ArticleView> = {};
  private _workflows: Record<StencilClient.WorkflowId, Composer.WorkflowView> = {};
  private _links: Record<StencilClient.LinkId, Composer.LinkView> = {};

  private _pagesByArticle: Record<StencilClient.ArticleId, Composer.PageView[]> = {};
  private _linksByArticle: Record<StencilClient.ArticleId, Composer.LinkView[]> = {};
  private _workflowsByArticle: Record<StencilClient.ArticleId, Composer.WorkflowView[]> = {};

  constructor(site: StencilClient.Site) {
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
  private visitPage(page: StencilClient.Page) {
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

  private visitLink(link: StencilClient.Link) {
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
  private visitWorkflow(workflow: StencilClient.Workflow) {
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
  private visitArticle(article: StencilClient.Article) {
    const articleId = article.id;
    const site = this._site;
    const pages: Composer.PageView[] = Object.values(site.pages)
      .filter(page => articleId === page.body.article)
      .map(page => new ImmutablePageView({ page, locale: site.locales[page.body.locale] }));

    const links: Composer.LinkView[] = this.empty(this._linksByArticle[articleId]);
    const workflows: Composer.WorkflowView[] = this.empty(this._workflowsByArticle[articleId]);

    const canCreate: StencilClient.SiteLocale[] = Object.values(site.locales).filter(locale => pages.filter(p => p.page.body.locale === locale.id).length === 0);
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


class ImmutableSessionFilter implements Composer.SessionFilter {
  private _locale?: string;

  constructor(props: {
    locale?: string
  }) {
    this._locale = props.locale;
  }

  get locale() {
    return this._locale;
  }
  withLocale(locale?: StencilClient.LocaleId) {
    return new ImmutableSessionFilter({ locale });
  }
}

class SessionData implements Composer.Session {
  private _site: StencilClient.Site;
  private _pages: Record<StencilClient.PageId, Composer.PageUpdate>;
  private _cache: SiteCache;
  private _filter: Composer.SessionFilter;

  constructor(props: {
    site?: StencilClient.Site,
    pages?: Record<StencilClient.PageId, Composer.PageUpdate>,
    cache?: SiteCache;
    filter?: Composer.SessionFilter;
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
  getArticleName(articleId: StencilClient.ArticleId) {
    return this.getArticleNameInternal(articleId, []);
  }
  getArticleNameInternal(articleId: StencilClient.ArticleId, visited: StencilClient.ArticleId[]) {
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
  getWorkflowName(workflowId: StencilClient.WorkflowId) {
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
  getLinkName(workflowId: StencilClient.WorkflowId) {
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
  getArticleView(articleId: StencilClient.ArticleId): Composer.ArticleView {
    return this._cache.getArticles()[articleId];
  }
  getWorkflowView(workflowId: StencilClient.WorkflowId): Composer.WorkflowView {
    return this._cache.getWorkflows()[workflowId];
  }
  getLinkView(linkId: StencilClient.LinkId): Composer.LinkView {
    return this._cache.getLinks()[linkId];
  }

  getArticlesForLocale(locale: StencilClient.LocaleId): StencilClient.Article[] {
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
  getArticlesForLocales(locales: StencilClient.LocaleId[]): StencilClient.Article[] {
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

  withSite(site: StencilClient.Site) {
    return new SessionData({ site: site, pages: this._pages, filter: this._filter });
  }
  withoutPages(pageIds: StencilClient.PageId[]): Composer.Session {
    const pages = {};
    for (const page of Object.values(this._pages)) {
      if (pageIds.includes(page.origin.id)) {
        continue;
      }
      pages[page.origin.id] = page;
    }
    return new SessionData({ site: this._site, pages, cache: this._cache, filter: this._filter });
  }
  withPage(page: StencilClient.PageId): Composer.Session {
    if (this._pages[page]) {
      return this;
    }
    const pages = Object.assign({}, this._pages);
    const origin = this._site.pages[page];
    pages[page] = new ImmutablePageUpdate({ origin, saved: true, value: origin.body.content });
    return new SessionData({ site: this._site, pages, cache: this._cache, filter: this._filter });
  }
  withPageValue(page: StencilClient.PageId, value: StencilClient.LocalisedContent): Composer.Session {
    const session = this.withPage(page);
    const pageUpdate = session.pages[page];

    const pages = Object.assign({}, session.pages);
    pages[page] = pageUpdate.withValue(value);

    return new SessionData({ site: session.site, pages, cache: this._cache, filter: this._filter });
  }

  withLocaleFilter(locale?: StencilClient.LocaleId) {
    return new SessionData({ site: this._site, pages: this._pages, cache: this._cache, filter: this._filter.withLocale(locale) });
  }
}

class ImmutablePageUpdate implements Composer.PageUpdate {
  private _saved: boolean;
  private _origin: StencilClient.Page;
  private _value: StencilClient.LocalisedContent;

  constructor(props: {
    saved: boolean;
    origin: StencilClient.Page;
    value: StencilClient.LocalisedContent;
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
  withValue(value: StencilClient.LocalisedContent): Composer.PageUpdate {
    return new ImmutablePageUpdate({ saved: false, origin: this._origin, value });
  }
}

class ImmutableTabData implements Composer.TabData {
  private _nav: Composer.Nav;

  constructor(props: { nav: Composer.Nav }) {
    this._nav = props.nav;
  }
  get nav() {
    return this._nav;
  }
  withNav(nav: Composer.Nav) {
    return new ImmutableTabData({
      nav: {
        type: nav.type,
        value: nav.value === undefined ? this._nav.value : nav.value,
        value2: nav.value2 === undefined ? this._nav.value2 : nav.value2
      }
    });
  }
}

class ImmutableArticleView implements Composer.ArticleView {
  private _article: StencilClient.Article;
  private _pages: Composer.PageView[];
  private _canCreate: StencilClient.SiteLocale[];
  private _links: Composer.LinkView[];
  private _workflows: Composer.WorkflowView[];
  private _children: Composer.ArticleView[];
  private _displayOrder: number;
  constructor(props: {
    article: StencilClient.Article;
    pages: Composer.PageView[];
    canCreate: StencilClient.SiteLocale[];
    links: Composer.LinkView[];
    workflows: Composer.WorkflowView[];
    children: Composer.ArticleView[];
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
  get children(): Composer.ArticleView[] { return this._children };
  get article(): StencilClient.Article { return this._article };
  get pages(): Composer.PageView[] { return this._pages };
  get canCreate(): StencilClient.SiteLocale[] { return this._canCreate };
  get links(): Composer.LinkView[] { return this._links };
  get workflows(): Composer.WorkflowView[] { return this._workflows };
  getPageById(id: StencilClient.PageId): Composer.PageView {
    const found = this._pages.find(p => p.page.id === id);
    if(!found) {
      throw new Error(`No page with page id: {id}!`);
    }
    return found;
  }
  getPageByLocaleId(id: StencilClient.LocaleId): Composer.PageView {
    const found = this.findPageByLocaleId(id);
    if(!found) {
      throw new Error(`No page with locale id: {id}!`);
    }
    return found;
  }
  findPageByLocaleId(id: StencilClient.LocaleId): Composer.PageView | undefined {
    return this._pages.find(p => p.page.body.locale === id);
  }
}

class ImmutablePageView implements Composer.PageView {
  private _page: StencilClient.Page;
  private _locale: StencilClient.SiteLocale;
  private _title: string;

  constructor(props: {
    page: StencilClient.Page;
    locale: StencilClient.SiteLocale;
  }) {
    this._page = props.page;
    this._locale = props.locale;
    this._title = this.getTitle(props.page);
  }

  private getTitle(page: StencilClient.Page) {
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
  get page(): StencilClient.Page { return this._page };
  get locale(): StencilClient.SiteLocale { return this._locale };
}


class ImmutableLinkView implements Composer.LinkView {
  private _link: StencilClient.Link;
  private _labels: Composer.LabelView[];

  constructor(props: {
    link: StencilClient.Link;
    labels: Composer.LabelView[];
  }) {
    this._link = props.link;
    this._labels = props.labels;
  }

  get link(): StencilClient.Link { return this._link };
  get labels(): Composer.LabelView[] { return this._labels };
}

class ImmutableWorkflowView implements Composer.WorkflowView {
  private _workflow: StencilClient.Workflow;
  private _labels: Composer.LabelView[];

  constructor(props: {
    workflow: StencilClient.Workflow;
    labels: Composer.LabelView[];
  }) {
    this._workflow = props.workflow;
    this._labels = props.labels;
  }

  get workflow(): StencilClient.Workflow { return this._workflow };
  get labels(): Composer.LabelView[] { return this._labels };
}

class ImmutableLabelView implements Composer.LabelView {
  private _label: StencilClient.LocaleLabel;
  private _locale: StencilClient.SiteLocale;

  constructor(props: {
    label: StencilClient.LocaleLabel;
    locale: StencilClient.SiteLocale;
  }) {
    this._label = props.label;
    this._locale = props.locale;
  }

  get locale(): StencilClient.SiteLocale { return this._locale };
  get label(): StencilClient.LocaleLabel { return this._label };
}



class ImmutableArticleSearchEntry implements Composer.SearchDataEntry {
  private _id: string;
  private _values: Composer.SearchableValue[];


  constructor(props: {
    id: StencilClient.ArticleId;
    values: Composer.SearchableValue[];
  }) {
    this._id = props.id;
    this._values = props.values;
    this._values.sort((e1, e2) => e1.type.localeCompare(e2.type));
  }
  get id() { return this._id }
  get values() { return this._values }
  get type(): "ARTICLE" { return "ARTICLE" };

  withPage(view: Composer.PageView): ImmutableArticleSearchEntry {
    const values: Composer.SearchableValue[] = [...this.values];
    values.push({ type: "ARTICLE_PAGE", value: view.page.body.content, id: view.page.id });
    return new ImmutableArticleSearchEntry({ id: this._id, values });
  }

  withArticle(view: Composer.ArticleView): ImmutableArticleSearchEntry {
    const values: Composer.SearchableValue[] = [...this.values];
    values.push({ type: "ARTICLE_NAME", value: view.article.body.name, id: view.article.id });
    return new ImmutableArticleSearchEntry({ id: this._id, values });
  }
}

class ImmutableLinkSearchEntry implements Composer.SearchDataEntry {
  private _id: string;
  private _values: Composer.SearchableValue[];


  constructor(props: {
    id: StencilClient.LinkId;
    values: Composer.SearchableValue[];
  }) {
    this._id = props.id;
    this._values = props.values;
  }
  get id() { return this._id }
  get values() { return this._values }
  get type(): "LINK" { return "LINK" };

  withLink(view: Composer.LinkView): ImmutableLinkSearchEntry {
    const values: Composer.SearchableValue[] = [...this.values];
    values.push({ type: "LINK_VALUE", value: view.link.body.value, id: view.link.id });

    for (const label of view.labels) {
      values.push({ type: "LINK_LABEL", value: label.label.labelValue, id: label.locale.id });
    }
    return new ImmutableLinkSearchEntry({ id: this._id, values });
  }
}



class ImmutableWorkflowSearchEntry implements Composer.SearchDataEntry {
  private _id: string;
  private _values: Composer.SearchableValue[];

  constructor(props: {
    id: StencilClient.WorkflowId;
    values: Composer.SearchableValue[];
  }) {
    this._id = props.id;
    this._values = props.values;
  }
  get id() { return this._id }
  get values() { return this._values }
  get type(): "WORKFLOW" { return "WORKFLOW" };

  withWorkflow(view: Composer.WorkflowView): ImmutableWorkflowSearchEntry {
    const values: Composer.SearchableValue[] = [...this.values];
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


export { SessionData, ImmutableTabData, SiteCache };
