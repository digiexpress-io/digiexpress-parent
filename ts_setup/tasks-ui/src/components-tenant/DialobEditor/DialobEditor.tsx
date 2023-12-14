import React from 'react';
import { Dialog } from '@mui/material';

import { TenantEntryDescriptor } from 'descriptor-tenant';
import Context from 'context';
import { DialobForm, Backend } from 'client';

import { DialobInit } from './DialobInit';



function getApiUrl(backend: Backend) {
  const ext = backend.config.urls.find(url => url.id === 'EXT_DIALOB_EDIT');
  if (ext) {
    return ext.url;
  }

  const local = backend.config.urls.find(url => url.id === 'DIALOB');
  if (local) {
    return local.url;
  }
  throw new Error("Dialob not configured! " + backend.config);
}

const DialobEditor: React.FC<{
  onClose: () => void,
  entry: TenantEntryDescriptor,
  form: DialobForm | undefined,

}> = ({ form, entry, onClose }) => {

  const backend = Context.useBackend();
  const { tenantConfig } = Context.useTenantConfig();
  const apiUrl = getApiUrl(backend);
  const tenantId = entry.tenantId;
  const formId: string = form?._id ?? '';
  const Composer = React.useCallback(() => <DialobInit
    apiUrl={apiUrl}
    dialobOnly={tenantConfig?.repoConfigs.length === 0 ? false : true}
    tenantId={tenantId}
    formId={formId}
    onClose={onClose}
  />, [apiUrl, tenantId, formId, tenantConfig, onClose]);

  if (!form) {
    return null;
  }

  return (<Dialog open={true} fullScreen sx={{ zIndex: 1300 }}>
    <React.Suspense fallback={<>loading composer...</>}>
      <Composer />
    </React.Suspense>
  </Dialog>);
}



export { DialobEditor };