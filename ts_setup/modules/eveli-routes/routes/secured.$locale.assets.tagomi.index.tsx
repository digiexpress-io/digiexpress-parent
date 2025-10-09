import { createFileRoute } from '@tanstack/react-router'
import React from 'react'

import { useFetch } from '@dxs-ts/envir-fetch';
import { StencilApi, StencilComposerApi } from '@dxs-ts/stencil-api';
import { parseStencilSearchParams, StencilRouteSearchParams, StencilSetup, StencilStickySave } from '@dxs-ts/stencil-routes';
import { EveliSetup } from '../eveli-setup';
import { EveliApp } from '../eveli-app';

export const Route = createFileRoute('/secured/$locale/assets/tagomi/')({
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
  const { getSite } = useFetch('worker/rest/api/assets/stencil.GET', {});
  const { delete: del } = useFetch('worker/rest/api/assets/stencil/$assetType.DELETE', {});
  const { create } = useFetch('worker/rest/api/assets/stencil/$assetType.POST', {});
  const { update } = useFetch('worker/rest/api/assets/stencil/$assetType.PUT', {});
  const { getReleaseContent } = useFetch('worker/rest/api/assets/stencil/releases/$releaseId.GET', {});
  const { getSiteCommitLog } = useFetch('worker/rest/api/assets/stencil/commitlogs.GET', {})

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