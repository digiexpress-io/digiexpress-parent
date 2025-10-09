import React from 'react';
import { createFileFetch } from '@dxs-ts/envir-fetch';
import { TagomiApi } from '@dxs-ts/tagomi-api';



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
    return this.fetch('/', 'GET');
  }

  getSites(): Promise<TagomiApi.TagomiContainer> {
    return this.fetch('/sites', 'GET');
  }

  // Resource operations
  createResource(body: TagomiApi.CreateResource): Promise<TagomiApi.Resource> {
    return this.fetch('/resources', 'POST', body);
  }

  updateResource(body: TagomiApi.ResourceMutator): Promise<TagomiApi.Resource> {
    return this.fetch('/resources', 'PUT', body);
  }

  deleteResource(linkId: string, articleId?: string): Promise<TagomiApi.Resource> {
    const query = articleId ? `?articleId=${encodeURIComponent(articleId)}` : '';
    return this.fetch(`/resources/${linkId}${query}`, 'DELETE');
  }

  // Service operations
  createService(body: TagomiApi.CreateService): Promise<TagomiApi.Service> {
    return this.fetch('/services', 'POST', body);
  }

  updateService(body: TagomiApi.ServiceMutator): Promise<TagomiApi.Service> {
    return this.fetch('/services', 'PUT', body);
  }

  deleteService(serviceId: string): Promise<TagomiApi.Service> {
    return this.fetch(`/services/${serviceId}`, 'DELETE');
  }

  // Locale operations
  createLocale(body: TagomiApi.CreateLocale): Promise<TagomiApi.Locale> {
    return this.fetch('/locales', 'POST', body);
  }

  updateLocale(body: TagomiApi.LocaleMutator): Promise<TagomiApi.Locale> {
    return this.fetch('/locales', 'PUT', body);
  }

  deleteLocale(id: string): Promise<TagomiApi.Locale> {
    return this.fetch(`/locales/${id}`, 'DELETE');
  }

  // Template operations
  createTemplate(body: TagomiApi.CreateTemplate): Promise<TagomiApi.Template> {
    return this.fetch('/templates', 'POST', body);
  }

  updateTemplate(body: TagomiApi.TemplateMutator[]): Promise<TagomiApi.Template[]> {
    return this.fetch('/templates', 'PUT', body);
  }

  deleteTemplate(id: string): Promise<TagomiApi.Template> {
    return this.fetch(`/templates/${id}`, 'DELETE');
  }

  // Tag operations
  createTag(body: TagomiApi.CreateTag): Promise<TagomiApi.Tag> {
    return this.fetch('/tags', 'POST', body);
  }

  deleteTag(id: string): Promise<TagomiApi.Tag> {
    return this.fetch(`/tags/${id}`, 'DELETE');
  }
}

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  const baseUrl = url({});

  const backend: TagomiApi.Backend = React.useMemo(() => new TagomiRestApiImpl(baseUrl), [baseUrl])
  return { backend }
}