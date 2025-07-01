import * as backend from '.';
import { DialobAdminConfig } from '../types';
import { getHeaders, useFetchAuth } from '../util';

export const useAdminBackend = (config: DialobAdminConfig) => {
  const fetchAuth = useFetchAuth();

  const getAdminFormConfigurationList = async () => {
    const url = backend.buildFormListOrCreateUrl(config);
    return fetchAuth(url, { method: 'GET' }, config);
  };

  const getAdminFormConfigurationTags = async (formId: string) => {
    const url = backend.buildFormTagsUrl(formId, config);
    return fetchAuth(url, { method: 'GET' }, config);
  };

  const getAdminFormAllTags = async () => {
    const url = backend.buildAllTagsUrl(config);
    return fetchAuth(url, { method: 'GET' }, config);
  };

  const getAdminFormConfiguration = async (formId: string) => {
    const url = backend.buildSingleFormUrl(formId, config);
    return fetchAuth(url, { method: 'GET' }, config);
  };

  const addAdminFormConfiguration = async (form: any) => {
    const url = backend.buildFormListOrCreateUrl(config);
    return fetchAuth(url, {
      method: 'POST',
      body: JSON.stringify(form)
    }, config);
  };

  const addAdminFormConfigurationFromCsv = async (csvData: string) => {
    const url = backend.buildFormListOrCreateUrl(config);
    const headers = getHeaders(config);
    return fetchAuth(url, {
      method: 'POST',
      headers: {
        ...headers,
        'Content-Type': 'text/csv',
      },
      body: csvData
    }, config);
  };

  const editAdminFormConfiguration = async (form: any) => {
    const url = backend.buildEditFormUrl(form.name, config);
    return fetchAuth(url, {
      method: 'PUT',
      body: JSON.stringify(form)
    }, config);
  };

  const deleteAdminFormConfiguration = async (formId: string) => {
    const url = backend.buildSingleFormUrl(formId, config);
    return fetchAuth(url, {
      method: 'DELETE'
    }, config);
  };

  return {
    getAdminFormConfigurationList,
    getAdminFormConfigurationTags,
    getAdminFormAllTags,
    getAdminFormConfiguration,
    addAdminFormConfiguration,
    addAdminFormConfigurationFromCsv,
    editAdminFormConfiguration,
    deleteAdminFormConfiguration,
  };
};