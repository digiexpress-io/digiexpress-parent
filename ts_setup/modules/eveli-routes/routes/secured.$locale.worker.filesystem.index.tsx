import React from 'react';
import { createFileRoute } from '@tanstack/react-router';
import { EveliSetup } from '../eveli-setup';
import { FsSetup } from '@dxs-ts/fs-composer';
import { EveliApp } from '../eveli-app';
import { FsDirentProvider, FsDirentProviderProps } from '@dxs-ts/fs-api';

import { parseFsSearchParams, FsRouteSearchParams, FsNavProvider } from '@dxs-ts/fs-nav';
import { useFetch } from '@dxs-ts/envir-fetch';

export const Route = createFileRoute('/secured/$locale/worker/filesystem/')({
  component: Component,
  validateSearch: (search: Record<string, unknown>): FsRouteSearchParams => parseFsSearchParams(search),
})

const MergedToolbar: React.FC = () => {
  return (<EveliSetup.Toolbar />);
};



function Component() {
  const { backend } = useFetch('worker/rest/api/assets/tagomi.GET', {});
  const { getDirents } = useFetch('worker/rest/api/assets/fs.GET', {});
  const { getDirentBody } = useFetch('worker/rest/api/assets/fs/dirents/$id/bodies/$bodyType.GET', {});
  const { applyTransientChanges } = useFetch('worker/rest/api/assets/fs/dirents/$id/bodies/$bodyType/transient-changes.POST', {});
  const { debugDirent } = useFetch('worker/rest/api/assets/fs/debugs.POST', {});
  const { putAny } = useFetch('worker/rest/api/assets/fs/dirents.PUT', {});
  const { putDescription } = useFetch('worker/rest/api/assets/fs/dirents/description/$id.PUT', {});
  const { putName } = useFetch('worker/rest/api/assets/fs/dirents/name/$id.PUT', {});
  const { postCopy } = useFetch('worker/rest/api/assets/fs/dirents/copy/$id.POST', {});
  const { putLabels } = useFetch('worker/rest/api/assets/fs/dirents/labels/$id.PUT', {});
  const { postAny } = useFetch('worker/rest/api/assets/fs/dirents.POST', {});
  const { deleteAny } = useFetch('worker/rest/api/assets/fs/dirents/$id.DELETE', {});

  const persistenceUnit: FsDirentProviderProps['persistenceUnit'] = {
    compilePrintoutPage: (serviceId, localeCode) => backend.compileTemplate(serviceId, localeCode, {}),
    fetchDirents: getDirents,
    fetchDirentBody: getDirentBody,
    applyTransientChanges,
    debugDirent,
    pushChange: async (change) => putAny(change.getCurrentProps()),
    pushCreate: async (change) => postAny(change.getCurrentProps()),
    pushDescription: putDescription,
    pushName: putName,
    pushCopy: async (props) => postCopy(props),
    pushLabels: putLabels,
    deleteDirent: deleteAny,
  };

  return (
    <FsDirentProvider persistenceUnit={persistenceUnit}>
        <EveliApp
          main={FsSetup.Main}
          secondary={FsSetup.Secondary}
          toolbar={MergedToolbar}
          wrapper={FsNavProvider}
          drawerWidth={450}
      />
    </FsDirentProvider>
  );
}