import React from 'react';
import { TenantFeature, useTenantConfig } from './TenantConfigContext';
import { EveliFeatureMapping, EveliFeatureType } from './EveliFeatureMapping';




export const EveliTenantFeatureEnabled: React.FC<{ children: React.ReactNode, id: EveliFeatureType }> = ({ children, id }) => {
  const { features } = useTenantConfig();
  const required = EveliFeatureMapping[id];
  const isEnabled = required(features);

  if (isEnabled) {
    return <>{children}</>
  }
  return (<></>)
}