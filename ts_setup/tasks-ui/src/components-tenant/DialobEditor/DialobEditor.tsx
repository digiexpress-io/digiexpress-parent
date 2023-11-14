import React from 'react';
import { Dialog } from '@mui/material';

import { TenantEntryDescriptor } from 'descriptor-tenant';
import Context from 'context';
import { DialobForm } from 'client';

import { DialobInit } from './DialobInit';
const DialobCss = React.lazy(() => import('./DialobCss'));

const DialobEditor: React.FC<{
  onClose: () => void,
  entry: TenantEntryDescriptor,
  form: DialobForm | undefined,

}> = ({ form, entry, onClose }) => {

  const backend = Context.useBackend();
  const apiUrl: string = backend.config.urls.find(url => url.id === 'dialob')?.url + "api" ?? '';
  const tenantId = entry.tenantId;
  const formId: string = form?._id ?? '';
  

  const Composer = React.useCallback(() => <DialobInit apiUrl={apiUrl} tenantId={tenantId} formId={formId} onClose={onClose}/>, [apiUrl, tenantId, formId, onClose]);

  if (!form) {
    return null;
  }  

  return (<Dialog open={true} fullScreen sx={{zIndex: 1300}}>
    <React.Suspense fallback={<>loading composer...</>}>
      <DialobCss />&& <Composer /> 
    </React.Suspense>
  </Dialog>);
}



export { DialobEditor };