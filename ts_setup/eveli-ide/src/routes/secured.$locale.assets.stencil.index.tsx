import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useLocale } from '@/api-locale'


import { useFetch } from '@dxs-ts/eveli-fetch';
import { StencilApi } from '@/api-stencil';
import { parseStencilSearchParams, StencilRouteSearchParams } from '@/stencil-nav';
import { StencilComposerApi, StencilSteup } from '@/stencil-setup';
import { EveliApp } from '@/eveli-app';

export const Route = createFileRoute('/secured/$locale/assets/stencil/')({
  component: Component,
  validateSearch: (search: Record<string, unknown>): StencilRouteSearchParams => parseStencilSearchParams(search)
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  const { getSite } = useFetch('worker/rest/api/assets/stencil.GET', {});
  const { delete: del } = useFetch('worker/rest/api/assets/stencil/$assetType.DELETE', {});
  const { create } = useFetch('worker/rest/api/assets/stencil/$assetType.POST', {});
  const { update } = useFetch('worker/rest/api/assets/stencil/$assetType.PUT', {});
  const { getReleaseContent } = useFetch('worker/rest/api/assets/stencil/releases/$releaseId.GET', {});

  React.useLayoutEffect(() => setLocale(locale), [locale])

  const service = React.useMemo(() => {
    const store: StencilApi.StencilRestApi = {getSite, delete: del, create, update, getReleaseContent};
    return StencilApi.service({ store });
  }, [getSite, del, create, update, getReleaseContent]);

  return (
    <StencilComposerApi.Provider service={service} >
      <EveliApp 
        tabs={StencilSteup.Tabs}
        main={StencilSteup.Main} 
        secondary={StencilSteup.Secondary} 
        toolbar={StencilSteup.Toolbar} 
      />
    </StencilComposerApi.Provider>)
}