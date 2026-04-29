import React from 'react';
import { createFileRoute } from '@tanstack/react-router';
import { EveliSetup } from '../eveli-setup';
import { FsSetup } from '@dxs-ts/fs-composer';
import { EveliApp } from '../eveli-app';
import { FsNavProvider, FsDirentProvider, FsDirentProviderProps } from '@dxs-ts/fs-api';
import { FsSearchProvider } from '../../fs-composer/fs-search';
import { useFetch } from '@dxs-ts/envir-fetch';

export const Route = createFileRoute('/secured/$locale/worker/filesystem/')({
  component: Component,
})

const MergedToolbar: React.FC = () => {
  return (<EveliSetup.Toolbar />);
};

function Component() {
  const { getDirents } = useFetch('worker/rest/api/assets/fs.GET', {});
  const { getDirentBody } = useFetch('worker/rest/api/assets/fs/dirents/$id/bodies/$bodyType.GET', {});

  const persistenceUnit: FsDirentProviderProps['persistenceUnit'] = {
    fetchDirents: getDirents,
    fetchDirentBody: getDirentBody
  };

  return (
    <FsDirentProvider persistenceUnit={persistenceUnit}>
      <FsNavProvider>
        <FsSearchProvider>
          <EveliApp
            main={FsSetup.Main}
            secondary={FsSetup.Secondary}
            toolbar={MergedToolbar}
            drawerWidth={450}
          />
        </FsSearchProvider>
      </FsNavProvider>
    </FsDirentProvider>
  );
}