import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useLocale } from '@/burger'

import {
  Composer, StencilApi,
  StencilClient, StencilComponents,
  StencilRouteSearchParams, parseStencilSearchParams,
} from '../stencil';
import { useFetch } from '@dxs-ts/eveli-fetch';
import { EveliApp } from '@/burger';

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
    return StencilClient.service({ store });
  }, [getSite, del, create, update, getReleaseContent]);

  return (
    <Composer.Provider service={service} >
      <EveliApp 
        tabs={StencilComponents.Tabs}
        main={StencilComponents.Main} 
        secondary={StencilComponents.Secondary} 
        toolbar={StencilComponents.Toolbar} 
      />
    </Composer.Provider>)
}