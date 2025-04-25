import React from 'react';
import { TenantFeature, useTenantConfig } from './TenantConfigContext';

function oneOf(required: TenantFeature[]): (given: TenantFeature[]) => boolean {
  return (given: TenantFeature[]) => {
    return !!given.find((givenOnFeature) => required.includes(givenOnFeature));
  };
}

function notOneOf(required: TenantFeature[]): (given: TenantFeature[]) => boolean {
  return (given: TenantFeature[]) => {
    return !given.find((givenOnFeature) => required.includes(givenOnFeature));
  };
}


const EveliFeatureMapping = {
  'LOGIN_BUTTON': notOneOf(['wrench-only']),
  'STENCIL_LOCALE_FILTER': oneOf(['stencil_locale_filter']),

  'WRENCH_RELEASES': notOneOf(['eveli_publication_only']),
  'STENCIL_RELEASES': notOneOf(['eveli_publication_only']),

  'FORM_REVIEW_TASK_HEADER': oneOf(['task_review_for_blind']),
  'FORM_REVIEW_BUTTON_BAR': notOneOf(['task_review_for_blind']),
}


export type EveliFeatureType = keyof typeof EveliFeatureMapping;


export const EveliTenantFeatureEnabled: React.FC<{ children: React.ReactNode, id: EveliFeatureType }> = ({ children, id }) => {
  const { features } = useTenantConfig();
  const required = EveliFeatureMapping[id];
  const isEnabled = required(features);

  if (isEnabled) {
    return <>{children}</>
  }
  return (<></>)
}