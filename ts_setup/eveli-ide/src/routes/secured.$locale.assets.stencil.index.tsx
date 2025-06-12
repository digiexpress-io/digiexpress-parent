import React from 'react'

import { useLocale } from '@/api-locale'
import { useFetch } from '@dxs-ts/eveli-fetch';
import { StencilApi } from '@/api-stencil';
import { parseStencilSearchParams, StencilRouteSearchParams } from '@/stencil-nav';
import { StencilComposerApi, StencilSetup } from '@/stencil-setup';
import { EveliSetup } from '@/eveli-setup';
import { EveliApp } from '@/eveli-app';
import { StencilStickySave } from '@/stencil-sticky-save';

export const Route = createFileRoute({
  component: Component,
  validateSearch: (search: Record<string, unknown>): StencilRouteSearchParams => parseStencilSearchParams(search)
})


const MergedToolbar: React.FC = () => {
  return <>
    <EveliSetup.Toolbar />
    <StencilSetup.Toolbar />
  </>
}

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  const { getSite } = useFetch('worker/rest/api/assets/stencil.GET', {});
  const { delete: del } = useFetch('worker/rest/api/assets/stencil/$assetType.DELETE', {});
  const { create } = useFetch('worker/rest/api/assets/stencil/$assetType.POST', {});
  const { update } = useFetch('worker/rest/api/assets/stencil/$assetType.PUT', {});
  const { getReleaseContent } = useFetch('worker/rest/api/assets/stencil/releases/$releaseId.GET', {});
  const { getSiteCommitLog } = useFetch('worker/rest/api/assets/stencil/commitlogs.GET', {})

  React.useLayoutEffect(() => setLocale(locale), [locale])

  const service = React.useMemo(() => {
    const store: StencilApi.StencilRestApi = { getSite, delete: del, create, update, getReleaseContent, getSiteCommitLog };
    return StencilApi.service({ store });
  }, [getSite, del, create, update, getReleaseContent]);

  return (
    <StencilComposerApi.Provider service={service} >
      <EveliApp 
        tabs={StencilSetup.Tabs} 
        main={StencilSetup.Main} 
        secondary={StencilSetup.Secondary} 
        toolbar={MergedToolbar}>

        <StencilStickySave />
      </EveliApp>
    </StencilComposerApi.Provider>)
}