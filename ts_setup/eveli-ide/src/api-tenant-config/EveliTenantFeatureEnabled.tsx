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
  'STENCIL_ENABLED': notOneOf(['stencil-disabled']),
  'WRENCH_ENABLED': notOneOf(['wrench-disabled']),
  'PUBLICATION_UPDATE': notOneOf(['external-deployment']),

  'STENCIL_LOCALE_FILTER': oneOf(['stencil_locale_filter']),

  'DIALOB_ENABLED': notOneOf(['wrench-disabled', 'stencil-disabled']),

  'WRENCH_RELEASES': notOneOf(['eveli_publication_only']),
  'STENCIL_RELEASES': notOneOf(['eveli_publication_only']),

  'FORM_REVIEW_FLASHY': oneOf(['visual_accommodation']),
  'FORM_REVIEW_NORMAL': notOneOf(['visual_accommodation']),

  'QUEUES_ENABLED': notOneOf(['queues-visually-disabled']),
  'FEEDBACK_ENABLED': notOneOf(['feedback-visually-disabled']),
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