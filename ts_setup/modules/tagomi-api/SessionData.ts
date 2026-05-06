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
    Object.values(site.services).sort((l0, l1) => l0.serviceName.localeCompare(l1.serviceName)).forEach(template => this.visitService(template))
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
  private visitTemplate(template: TagomiApi.Template) {
    const site = this._site;
    const resources = Object.values(this._site.resources).filter(r => r.templateIds.includes(template.id))
    const view = new ImmutableTemplateView({ template, locale: site.locales[template.localeId], resources });
    this._templates[template.id] = view;

    const serviceId = template.serviceId;
    let serviceTemplates = this._templatesByService[serviceId];
    if (!serviceTemplates) {
      serviceTemplates = [];
      this._templatesByService[serviceId] = serviceTemplates;
    }
    serviceTemplates.push(view);
  }

  private visitLink(link: TagomiApi.Resource) {
    const site = this._site;
    const view = new ImmutableResourceView({
      resource: link,
      templates: link.templateIds.map(l => this._templates[l])
    });

    this._resources[link.id] = view;
    for (const serviceId of link.templateIds) {
      let serviceLinks = this._resourcesByTemplate[serviceId];
      if (!serviceLinks) {
        serviceLinks = [];
        this._resourcesByTemplate[serviceId] = serviceLinks;
      }
      serviceLinks.push(view);
    }
  }
  private visitService(service: TagomiApi.Service) {
    const serviceId = service.id;
    const site = this._site;
    const templates: TagomiComposerApi.TemplateView[] = this._templatesByService[serviceId] ?? [];
    const resources: TagomiComposerApi.ResourceView[] = Array.from(new Map(templates
      .flatMap(t => this._resourcesByTemplate[t.template.id] ?? []).reverse()
      .map(item => [item.resource.id, item]))
      .values());

    const labels: TagomiComposerApi.LabelView[] = [];

    const canCreate: TagomiApi.Locale[] = Object.values(site.locales).filter(locale => templates.filter(p => p.locale.id === locale.id).length === 0);
    const view = new ImmutableServiceView({
      service,
      templates, 
      canCreate,
      resources,
      labels,
      displayOrder: 0
    });

    this._services[serviceId] = view;
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
    this._site = props.site ? props.site : { tagName: "", locales: {}, resources: {}, services: {}, templates: {} };
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
    const templates = Object.values(this._site.templates)
    return locale ? Object.values(this._site.services).filter(service => {
      for (const template of templates) {
        if (template.serviceId === service.id && template.localeId === locale) {
          return true;
        }
      }
      return false;
    }) : []
  }
  getServicesForLocales(locales: TagomiApi.LocaleId[]): TagomiApi.Service[] {
    const templates = Object.values(this._site.templates)
    return locales && locales.length > 0 ? Object.values(this._site.services).filter(service => {
      for (const template of templates) {
        if (template.serviceId === service.id && locales.includes(template.localeId)) {
          return true;
        }
      }
      return false;
    }) : []
  }

  withSite(site: TagomiApi.TagomiContainer): TagomiComposerApi.Session {
    return new SessionData({ site: site, templates: this._templates });
  }
  withoutTemplates(templateIds: TagomiApi.TemplateId[]): TagomiComposerApi.Session {
    const templates: Record<string, TagomiComposerApi.TemplateUpdate> = {};
    for (const template of Object.values(this._templates)) {
      if (templateIds.includes(template.origin.id)) {
        continue;
      }
      templates[template.origin.id] = template;
    }
    return new SessionData({ site: this._site, templates: templates, cache: this._cache });
  }
  withTemplate(template: TagomiApi.TemplateId): TagomiComposerApi.Session {
    if (this._templates[template]) {
      return this;
    }
    const templates = Object.assign({}, this._templates);
    const origin = this._site.templates[template];
    templates[template] = new ImmutablePageUpdate({ origin, saved: true, value: origin.content });
    return new SessionData({ site: this._site, templates: templates, cache: this._cache });
  }
  withTemplateValue(template: TagomiApi.TemplateId, value: TagomiApi.LocalisedContent): TagomiComposerApi.Session {
    const session = this.withTemplate(template);
    const templateUpdate = session.templates[template];

    const templates = Object.assign({}, session.templates);
    templates[template] = templateUpdate.withValue(value);

    return new SessionData({ site: session.site, templates: templates, cache: this._cache });
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
  private _service: TagomiApi.Service;
  private _templates: TagomiComposerApi.TemplateView[];
  private _canCreate: TagomiApi.Locale[];
  private _resources: TagomiComposerApi.ResourceView[];
  private _displayOrder: number;
  private _labels: TagomiComposerApi.LabelView[];

  constructor(props: {
    service: TagomiApi.Service;
    templates: TagomiComposerApi.TemplateView[];
    canCreate: TagomiApi.Locale[];
    resources: TagomiComposerApi.ResourceView[];
    labels: TagomiComposerApi.LabelView[];
    displayOrder: number;
  }) {
    this._service = props.service;
    this._templates = props.templates;
    this._canCreate = props.canCreate;
    this._resources = props.resources;
    this._labels = props.labels;
    this._displayOrder = props.displayOrder;
  }

  get displayOrder(): number { return this._displayOrder };
  get service(): TagomiApi.Service { return this._service };
  get templates(): TagomiComposerApi.TemplateView[] { return this._templates };
  get canCreate(): TagomiApi.Locale[] { return this._canCreate };
  get resources(): TagomiComposerApi.ResourceView[] { return this._resources };
  get labels(): TagomiComposerApi.LabelView[] { return this._labels };
  
  getTemplateById(id: TagomiApi.TemplateId): TagomiComposerApi.TemplateView {
    const found = this._templates.find(p => p.template.id === id);
    if(!found) {
      throw new Error(`No template with template id: {id}!`);
    }
    return found;
  }
  getTemplateByLocaleId(id: TagomiApi.LocaleId): TagomiComposerApi.TemplateView {
    const found = this.findTemplateByLocaleId(id);
    if(!found) {
      throw new Error(`No template with locale id: {id}!`);
    }
    return found;
  }
  findTemplateByLocaleId(id: TagomiApi.LocaleId): TagomiComposerApi.TemplateView | undefined {
    return this._templates.find(p => p.locale.id === id);
  }
}

class ImmutableTemplateView implements TagomiComposerApi.TemplateView {
  private _template: TagomiApi.Template;
  private _locale: TagomiApi.Locale;
  private _title: string;
  private _resources: TagomiApi.Resource[];

  constructor(props: {
    template: TagomiApi.Template;
    locale: TagomiApi.Locale;
    resources: TagomiApi.Resource[];
  }) {
    this._template = props.template;
    this._locale = props.locale;
    this._title = this.getTitle(props.template);
    this._resources = props.resources;
  }

  private getTitle(template: TagomiApi.Template) {
    const heading1 = template.content.indexOf("# ");

    if (heading1 === -1) {
      return template.content.substring(0, Math.min(template.content.length, 30));
    }
    const lineBreak1 = template.content.indexOf("\n", heading1)
    if (lineBreak1 > 0) {
      return template.content.substring(0, Math.min(lineBreak1, 30)).substring(2);
    }

    const lineBreak2 = template.content.indexOf("\r\n", heading1)
    if (lineBreak2 > 0) {
      return template.content.substring(0, Math.min(lineBreak2, 30)).substring(2);
    }

    return template.content.substring(2);

  }
  get resources(): TagomiApi.Resource[] { return this._resources };
  get title(): string { return this._title };
  get template(): TagomiApi.Template { return this._template };
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
