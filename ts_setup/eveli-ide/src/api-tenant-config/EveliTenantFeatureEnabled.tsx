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