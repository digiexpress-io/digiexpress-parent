import type { DialobAdminConfig } from '../types';

const buildUrlWithTenant = (baseUrl: string, config: DialobAdminConfig) => {
  if (!config.tenantId) return baseUrl;
  const separator = baseUrl.includes('?') ? '&' : '?';
  return `${baseUrl}${separator}tenantId=${config.tenantId}`;
};

export const buildFormListOrCreateUrl = (config: DialobAdminConfig) =>
  buildUrlWithTenant(`${config.dialobApiUrl}/api/forms`, config);

export const buildFormTagsUrl = (formId: string, config: DialobAdminConfig) =>
  buildUrlWithTenant(`${config.dialobApiUrl}/api/forms/${formId}/tags`, config);

export const buildAllTagsUrl = (config: DialobAdminConfig) =>
  buildUrlWithTenant(`${config.dialobApiUrl}/api/tags`, config);

export const buildSingleFormUrl = (formId: string, config: DialobAdminConfig) =>
  buildUrlWithTenant(`${config.dialobApiUrl}/api/forms/${formId}`, config);

export const buildEditFormUrl = (formName: string, config: DialobAdminConfig) =>
  buildUrlWithTenant(`${config.dialobApiUrl}/api/forms/${formName}?force=true`, config);