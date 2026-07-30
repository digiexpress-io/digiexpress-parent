import React from 'react';
import { createFileFetch } from '@dxs-ts/envir-fetch';
import { TagomiApi } from '@dxs-ts/tagomi-api';

type ModelEnvelope<B> = TagomiApi.Wire.ModelEnvelope<B>;
type LocaleBody = TagomiApi.Wire.LocaleBody;
type PrintoutPageBody = TagomiApi.Wire.PrintoutPageBody;
type PrintoutResourceBody = TagomiApi.Wire.PrintoutResourceBody;
type PrintoutBody = TagomiApi.Wire.PrintoutBody;
type ModelWorld = TagomiApi.Wire.ModelWorld;

const toLocale = (m: ModelEnvelope<LocaleBody>): TagomiApi.Locale => ({
  id: m.id,
  docType: 'LOCALE',
  localeCode: m.body.value,
  enabled: m.body.enabled,
  default: false,
});

const toTemplate = (m: ModelEnvelope<PrintoutPageBody>): TagomiApi.Template => ({
  id: m.id,
  docType: 'TEMPLATE',
  content: m.body.content,
  localeId: m.body.localeId,
  serviceId: m.body.serviceId,
  templateIds: m.body.printoutPageIds ?? [],
});

const toResource = (m: ModelEnvelope<PrintoutResourceBody>): TagomiApi.Resource => ({
  id: m.id,
  docType: 'RESOURCE',
  externalLocation: m.body.externalLocation,
  resourceName: m.body.resourceName,
  contentType: m.body.contentType as TagomiApi.ResourceContentType,
  templateIds: m.body.printoutPageIds ?? [],
  content: m.body.content,
});

const toService = (m: ModelEnvelope<PrintoutBody>): TagomiApi.Service => ({
  id: m.id,
  docType: 'SERVICE',
  serviceName: m.body.serviceName,
  orchestratorName: m.body.orchestratorName,
  labels: (m.body.labels ?? []).map(l => ({ locale: l.locale, labelValue: l.labelValue })),
});

const mapEnvelopes = <B, T>(
  source: Record<string, ModelEnvelope<B>> | undefined,
  fn: (m: ModelEnvelope<B>) => T,
): Record<string, T> => {
  const result: Record<string, T> = {};
  if (!source) {
    return result;
  }
  for (const [id, envelope] of Object.entries(source)) {
    result[id] = fn(envelope);
  }
  return result;
};

const toTagomiContainer = (world: ModelWorld): TagomiApi.TagomiContainer => ({
  tagName: world.name ?? '',
  commitId: world.commitId,
  locales: mapEnvelopes(world.locales, toLocale),
  templates: mapEnvelopes(world.printoutPages, toTemplate),
  resources: mapEnvelopes(world.printoutResources, toResource),
  services: mapEnvelopes(world.printouts, toService),
});


export const Hook = createFileFetch('worker/rest/api/assets/tagomi.GET')({
  hook
})

class TagomiRestApiImpl implements TagomiApi.Backend {
  private readonly baseUrl: string;
  constructor(baseUrl: string) {
    this.baseUrl = baseUrl;
  }

  private async fetch<T>(path: string, method: string, body?: any): Promise<T> {
    const url = `${this.baseUrl}${path}`;
    const init: RequestInit = {
      method,
      headers: {
        'Content-Type': 'application/json',
      },
    };

    if (body !== undefined) {
      init.body = JSON.stringify(body);
    }

    return fetch(url, init).then(resp => resp.json());
  }

  // Query operations
  getRoot(): Promise<TagomiApi.TagomiContainer> {
    return this.fetch<ModelWorld>('/', 'GET').then(toTagomiContainer);
  }

  getSites(): Promise<TagomiApi.TagomiContainer> {
    return this.fetch<ModelWorld>('/sites', 'GET').then(toTagomiContainer);
  }

  // Resource operations
  createResource(body: TagomiApi.CreateResource): Promise<TagomiApi.Resource> {
    const payload = {
      id: body.id,
      resourceName: body.resourceName,
      contentType: body.contentType,
      uploadBody: body.uploadBody,
      printoutPageIds: body.templateIds ?? [],
    };
    return this.fetch<ModelEnvelope<PrintoutResourceBody>>('/resources', 'POST', payload).then(toResource);
  }

  updateResource(body: TagomiApi.ResourceMutator): Promise<TagomiApi.Resource> {
    const payload = {
      resourceId: body.resourceId,
      resourceName: body.resourceName,
      contentType: body.contentType,
      uploadBody: body.uploadBody,
      printoutPageIds: body.templateIds,
    };
    return this.fetch<ModelEnvelope<PrintoutResourceBody>>('/resources', 'PUT', payload).then(toResource);
  }

  deleteResource(linkId: string, articleId?: string): Promise<TagomiApi.Resource> {
    const query = articleId ? `?articleId=${encodeURIComponent(articleId)}` : '';
    return this.fetch<ModelEnvelope<PrintoutResourceBody>>(`/resources/${linkId}${query}`, 'DELETE').then(toResource);
  }

  // Service operations
  createService(body: TagomiApi.CreateService): Promise<TagomiApi.Service> {
    return this.fetch<ModelEnvelope<PrintoutBody>>('/services', 'POST', body).then(toService);
  }

  updateService(body: TagomiApi.ServiceMutator): Promise<TagomiApi.Service> {
    return this.fetch<ModelEnvelope<PrintoutBody>>('/services', 'PUT', body).then(toService);
  }

  deleteService(serviceId: string): Promise<TagomiApi.Service> {
    return this.fetch<ModelEnvelope<PrintoutBody>>(`/services/${serviceId}`, 'DELETE').then(toService);
  }

  // Template operations
  createTemplate(body: TagomiApi.CreateTemplate): Promise<TagomiApi.Template> {
    const payload = {
      id: body.id,
      serviceId: body.serviceId,
      localeId: body.locale,
      content: body.content,
      printoutPageIds: body.templateIds ?? [],
    };
    return this.fetch<ModelEnvelope<PrintoutPageBody>>('/templates', 'POST', payload).then(toTemplate);
  }

  updateTemplate(body: TagomiApi.TemplateMutator[]): Promise<TagomiApi.Template[]> {
    return Promise.all(body.map(t => {
      const payload = {
        pageId: t.templateId,
        content: t.content,
        localeId: t.locale,
        resourceIds: t.resourceIds,
        printoutPageIds: t.templateIds,
      };
      return this.fetch<ModelEnvelope<PrintoutPageBody>>('/templates', 'PUT', payload).then(toTemplate);
    }));
  }

  deleteTemplate(id: string): Promise<TagomiApi.Template> {
    return this.fetch<ModelEnvelope<PrintoutPageBody>>(`/templates/${id}`, 'DELETE').then(toTemplate);
  }

  async compileTemplate(id: TagomiApi.ServiceId, locale: TagomiApi.LocaleId | string, props: object): Promise<TagomiApi.PdfEnvelope> {
    const url = `${this.baseUrl}/services/${id}/pdf/${locale}`;
    const resp = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(props),
    });
    if (!resp.ok) {
      const statusMessage = await resp.text().catch(() => `HTTP ${resp.status}`);
      return {
        name: '',
        localisedName: undefined,
        locale: String(locale),
        bodyBase64: '',
        status: 'ERROR',
        statusMessage,
      };
    }
    return resp.json();
  }
}

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  const baseUrl = url({});

  const backend: TagomiApi.Backend = React.useMemo(() => new TagomiRestApiImpl(baseUrl), [baseUrl])
  return { backend }
}