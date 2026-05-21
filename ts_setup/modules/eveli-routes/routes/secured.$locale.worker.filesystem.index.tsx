import React from 'react';
import { createFileRoute } from '@tanstack/react-router';
import { EveliSetup } from '../eveli-setup';
import { FsSetup, FsThemeProvider } from '@dxs-ts/fs-composer';
import { EveliApp } from '../eveli-app';
import { FsDirentProvider, FsDirentProviderProps } from '@dxs-ts/fs-api';
import { FsSearchProvider } from '../../fs-composer/fs-search';
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
  const { getDirents } = useFetch('worker/rest/api/assets/fs.GET', {});
  const { getDirentBody } = useFetch('worker/rest/api/assets/fs/dirents/$id/bodies/$bodyType.GET', {});
  const { applyTransientChanges } = useFetch('worker/rest/api/assets/fs/dirents/$id/bodies/$bodyType/transient-changes.POST', {});
  const { debugDirent } = useFetch('worker/rest/api/assets/fs/debugs.POST', {});
  const { putLink } = useFetch('worker/rest/api/assets/fs/dirents/links/$id.PUT', {});

  const persistenceUnit: FsDirentProviderProps['persistenceUnit'] = {
    fetchDirents: getDirents,
    fetchDirentBody: getDirentBody,
    applyTransientChanges,
    debugDirent,
    pushChange: async (change) => {
      const { bodyType, changes } = change.getCurrentProps();
      if (bodyType === 'ARTICLE_LINK') {
        await putLink({
          linkId: changes.id,
          value: changes.urlValue,
          type: changes.contentType,
          articles: changes.articles,
          labels: Object.entries(changes.intlValues as Record<string, string>).map(([locale, labelValue]) => ({ locale, labelValue })),
        });
      }
    }
  };

  return (
    <FsDirentProvider persistenceUnit={persistenceUnit}>
      <FsThemeProvider>
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
      </FsThemeProvider>
    </FsDirentProvider>
  );
}