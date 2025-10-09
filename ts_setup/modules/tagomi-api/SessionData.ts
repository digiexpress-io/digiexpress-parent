import { TagomiComposerApi } from './ide';
import { TagomiApi } from './TagomiApi';


class SiteCache {

  private _site: TagomiApi.TagomiContainer;
  private _services: Record<TagomiApi.ServiceId, TagomiComposerApi.ServiceView> = {};
  private _templates: Record<TagomiApi.TemplateId, TagomiComposerApi.TemplateView> = {};
  private _resources: Record<TagomiApi.ResourceId, TagomiComposerApi.ResourceView> = {};

  private _templatesByService: Record<TagomiApi.ServiceId, TagomiComposerApi.TemplateView[]> = {};
  private _resourcesByTemplate: Record<TagomiApi.TemplateId, TagomiComposerApi.ResourceView[]> = {};

  constructor(site: TagomiApi.TagomiContainer) {
    this._site = site;
    Object.values(site.templates).sort((l0, l1) => l0.id.localeCompare(l1.id)).forEach(link => this.visitTemplate(link))
    Object.values(site.resources).sort((l0, l1) => l0.resourceName.localeCompare(l1.resourceName)).forEach(workflow => this.visitLink(workflow))
    Object.values(site.services).sort((l0, l1) => l0.serviceName.localeCompare(l1.serviceName)).forEach(page => this.visitService(page))
  }
  getServices() {
    return this._services;
  }
  getResources() {
    return this._resources;
  }
  getTemplates() {
    return this._templates;
  }
  private visitTemplate(page: TagomiApi.Template) {
    const site = this._site;
    const resources = Object.values(this._site.resources).filter(r => r.templateIds.includes(page.id))
    const view = new ImmutableTemplateView({ page, locale: site.locales[page.localeId], resources });
    this._templates[page.id] = view;

    const articleId = page.serviceId;
    let articlePages = this._templatesByService[articleId];
    if (!articlePages) {
      articlePages = [];
      this._templatesByService[articleId] = articlePages;
    }
    articlePages.push(view);
  }

  private visitLink(link: TagomiApi.Resource) {
    const site = this._site;
    const view = new ImmutableResourceView({
      resource: link,
      templates: link.templateIds.map(l => this._templates[l])
    });

    this._resources[link.id] = view;
    for (const articleId of link.templateIds) {
      let articleLinks = this._resourcesByTemplate[articleId];
      if (!articleLinks) {
        articleLinks = [];
        this._resourcesByTemplate[articleId] = articleLinks;
      }
      articleLinks.push(view);
    }
  }
  private visitService(article: TagomiApi.Service) {
    const articleId = article.id;
    const site = this._site;
    const pages: TagomiComposerApi.TemplateView[] = this._templatesByService[articleId] ?? [];
    const resources: TagomiComposerApi.ResourceView[] = Array.from(new Map(pages
      .flatMap(t => this._resourcesByTemplate[t.template.id] ?? []).reverse()
      .map(item => [item.resource.id, item]))
      .values());

    const labels: TagomiComposerApi.LabelView[] = [];

    const canCreate: TagomiApi.Locale[] = Object.values(site.locales).filter(locale => pages.filter(p => p.locale.id === locale.id).length === 0);
    const view = new ImmutableServiceView({
      article, 
      pages, 
      canCreate,
      resources,
      labels,
      displayOrder: 0
    });

    this._services[articleId] = view;
  }
}


class SessionData implements TagomiComposerApi.Session {
  private _site: TagomiApi.TagomiContainer;
  private _templates: Record<TagomiApi.TemplateId, TagomiComposerApi.TemplateUpdate>;
  private _cache: SiteCache;

  constructor(props: {
    site?: TagomiApi.TagomiContainer,
    templates?: Record<TagomiApi.TemplateId, TagomiComposerApi.TemplateUpdate>,
    cache?: SiteCache;
  }) {
    this._site = props.site ? props.site : { tagName: "", locales: {}, resources: {}, services: {}, tags: {}, templates: {} };
    this._cache = props.cache ? props.cache : new SiteCache(this._site);
    this._templates = props.templates ?? {};
  }
  get services() {
    return Object.values(this._cache.getServices());
  }
  get site() {
    return this._site;
  }
  get templates() {
    return this._templates;
  }

  getServiceView(workflowId: TagomiApi.ServiceId): TagomiComposerApi.ServiceView {
    return this._cache.getServices()[workflowId];
  }

  getServicesForLocale(locale: TagomiApi.LocaleId): TagomiApi.Service[] {
    const pages = Object.values(this._site.templates)
    return locale ? Object.values(this._site.services).filter(article => {
      for (const page of pages) {
        if (page.serviceId === article.id && page.localeId === locale) {
          return true;
        }
      }
      return false;
    }) : []
  }
  getServicesForLocales(locales: TagomiApi.LocaleId[]): TagomiApi.Service[] {
    const pages = Object.values(this._site.templates)
    return locales && locales.length > 0 ? Object.values(this._site.services).filter(article => {
      for (const page of pages) {
        if (page.serviceId === article.id && locales.includes(page.localeId)) {
          return true;
        }
      }
      return false;
    }) : []
  }

  withSite(site: TagomiApi.TagomiContainer): TagomiComposerApi.Session {
    return new SessionData({ site: site, templates: this._templates });
  }
  withoutTemplates(pageIds: TagomiApi.TemplateId[]): TagomiComposerApi.Session {
    const pages: Record<string, TagomiComposerApi.TemplateUpdate> = {};
    for (const page of Object.values(this._templates)) {
      if (pageIds.includes(page.origin.id)) {
        continue;
      }
      pages[page.origin.id] = page;
    }
    return new SessionData({ site: this._site, templates: pages, cache: this._cache });
  }
  withTemplate(page: TagomiApi.TemplateId): TagomiComposerApi.Session {
    if (this._templates[page]) {
      return this;
    }
    const pages = Object.assign({}, this._templates);
    const origin = this._site.templates[page];
    pages[page] = new ImmutablePageUpdate({ origin, saved: true, value: origin.content });
    return new SessionData({ site: this._site, templates: pages, cache: this._cache });
  }
  withTemplateValue(page: TagomiApi.TemplateId, value: TagomiApi.LocalisedContent): TagomiComposerApi.Session {
    const session = this.withTemplate(page);
    const pageUpdate = session.templates[page];

    const pages = Object.assign({}, session.templates);
    pages[page] = pageUpdate.withValue(value);

    return new SessionData({ site: session.site, templates: pages, cache: this._cache });
  }
}

class ImmutablePageUpdate implements TagomiComposerApi.TemplateUpdate {
  private _saved: boolean;
  private _origin: TagomiApi.Template;
  private _value: TagomiApi.LocalisedContent;

  constructor(props: {
    saved: boolean;
    origin: TagomiApi.Template;
    value: TagomiApi.LocalisedContent;
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
  withValue(value: TagomiApi.LocalisedContent): TagomiComposerApi.TemplateUpdate {
    return new ImmutablePageUpdate({ saved: false, origin: this._origin, value });
  }
}


class ImmutableServiceView implements TagomiComposerApi.ServiceView {
  private _article: TagomiApi.Service;
  private _pages: TagomiComposerApi.TemplateView[];
  private _canCreate: TagomiApi.Locale[];
  private _resources: TagomiComposerApi.ResourceView[];
  private _displayOrder: number;
  private _labels: TagomiComposerApi.LabelView[];

  constructor(props: {
    article: TagomiApi.Service;
    pages: TagomiComposerApi.TemplateView[];
    canCreate: TagomiApi.Locale[];
    resources: TagomiComposerApi.ResourceView[];
    labels: TagomiComposerApi.LabelView[];
    displayOrder: number;
  }) {
    this._article = props.article;
    this._pages = props.pages;
    this._canCreate = props.canCreate;
    this._resources = props.resources;
    this._labels = props.labels;
    this._displayOrder = props.displayOrder;
  }

  get displayOrder(): number { return this._displayOrder };
  get service(): TagomiApi.Service { return this._article };
  get templates(): TagomiComposerApi.TemplateView[] { return this._pages };
  get canCreate(): TagomiApi.Locale[] { return this._canCreate };
  get resources(): TagomiComposerApi.ResourceView[] { return this._resources };
  get labels(): TagomiComposerApi.LabelView[] { return this._labels };
  
  getTemplateById(id: TagomiApi.TemplateId): TagomiComposerApi.TemplateView {
    const found = this._pages.find(p => p.template.id === id);
    if(!found) {
      throw new Error(`No page with page id: {id}!`);
    }
    return found;
  }
  getTemplateByLocaleId(id: TagomiApi.LocaleId): TagomiComposerApi.TemplateView {
    const found = this.findTemplateByLocaleId(id);
    if(!found) {
      throw new Error(`No page with locale id: {id}!`);
    }
    return found;
  }
  findTemplateByLocaleId(id: TagomiApi.LocaleId): TagomiComposerApi.TemplateView | undefined {
    return this._pages.find(p => p.locale.id === id);
  }
}

class ImmutableTemplateView implements TagomiComposerApi.TemplateView {
  private _page: TagomiApi.Template;
  private _locale: TagomiApi.Locale;
  private _title: string;
  private _resources: TagomiApi.Resource[];

  constructor(props: {
    page: TagomiApi.Template;
    locale: TagomiApi.Locale;
    resources: TagomiApi.Resource[];
  }) {
    this._page = props.page;
    this._locale = props.locale;
    this._title = this.getTitle(props.page);
    this._resources = props.resources;
  }

  private getTitle(page: TagomiApi.Template) {
    const heading1 = page.content.indexOf("# ");

    if (heading1 === -1) {
      return page.content.substring(0, Math.min(page.content.length, 30));
    }
    const lineBreak1 = page.content.indexOf("\n", heading1)
    if (lineBreak1 > 0) {
      return page.content.substring(0, Math.min(lineBreak1, 30)).substring(2);
    }

    const lineBreak2 = page.content.indexOf("\r\n", heading1)
    if (lineBreak2 > 0) {
      return page.content.substring(0, Math.min(lineBreak2, 30)).substring(2);
    }

    return page.content.substring(2);

  }
  get resources(): TagomiApi.Resource[] { return this._resources };
  get title(): string { return this._title };
  get template(): TagomiApi.Template { return this._page };
  get locale(): TagomiApi.Locale { return this._locale };
}


class ImmutableResourceView implements TagomiComposerApi.ResourceView {
  private _resource: TagomiApi.Resource;
  private _templates: TagomiComposerApi.TemplateView[];

  constructor(props: {
    resource: TagomiApi.Resource;
    templates: TagomiComposerApi.TemplateView[];
  }) {
    this._resource = props.resource;
    this._templates = props.templates;
  }

  get resource(): TagomiApi.Resource { return this._resource };
  get templates(): TagomiComposerApi.TemplateView[] { return this._templates };
}

export { SessionData, SiteCache };
