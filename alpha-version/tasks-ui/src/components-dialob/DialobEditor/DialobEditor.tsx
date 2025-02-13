import React from 'react';
import { Dialog } from '@mui/material';
import { Backend, useBackend } from 'descriptor-backend';
import { DialobInit } from './DialobInit';



function getApiUrl(backend: Backend) {
  const local = backend.config.urls['DIALOB'];
  if (local) {
    return local;
  }
  throw new Error("Dialob not configured! " + backend.config);
}

export const DialobEditor: React.FC<{
  onClose: () => void,
  entry: { tenantId: string },
  form: { _id: string } | undefined,

}> = ({ form, entry, onClose }) => {
  const backend = useBackend();
  
  const apiUrl = getApiUrl(backend);
  const tenantId = entry.tenantId;
  const formId: string = form?._id ?? '';
  const Composer = React.useCallback(() => <DialobInit
    apiUrl={apiUrl}
    dialobOnly={false}
    tenantId={tenantId}
    formId={formId}
    onClose={onClose}
  />, [apiUrl, tenantId, formId, onClose]);

  if (!form) {
    return null;
  }

  return (<Dialog open={true} fullScreen sx={{ zIndex: 1300 }}>
    <React.Suspense fallback={<>loading composer...</>}>
      <Composer />
    </React.Suspense>
  </Dialog>);
}