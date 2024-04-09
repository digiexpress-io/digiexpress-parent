import React from 'react';

import { TenantContext, TenantContextType } from 'descriptor-dialob';
export const useDialobTenant = () => {
  const result: TenantContextType = React.useContext(TenantContext);
  return result;
}