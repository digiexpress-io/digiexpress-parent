import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useLocale } from '@/burger'
import { StencilClient, Main, Toolbar, Secondary } from '../stencil';
import { Composer, StencilApi } from '../stencil/context';
import { useFetch } from '@dxs-ts/eveli-fetch';
import { EveliApp } from '@/burger';

export const Route = createFileRoute('/secured/$locale/assets/stencil/')({
  component: Component,
}) 

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();
  React.useLayoutEffect(() => setLocale(locale), [locale])

  const { getSite } = useFetch('worker/rest/api/assets/stencil.GET', {});
  const { delete: del } = useFetch('worker/rest/api/assets/stencil/$assetType.DELETE', {});
  const { create } = useFetch('worker/rest/api/assets/stencil/$assetType.POST', {});
  const { update } = useFetch('worker/rest/api/assets/stencil/$assetType.PUT', {});
  const { getReleaseContent } = useFetch('worker/rest/api/assets/stencil/releases/$releaseId.GET', {});
  const { version } = useFetch('worker/rest/api/assets/stencil/version.GET', {});

  const service = React.useMemo(() => {
    const store: StencilApi.StencilRestApi = {getSite, delete: del, create, update, getReleaseContent, version};
    return StencilClient.service({ store });
  }, [getSite, del, create, update, getReleaseContent, version]);
  
  return (
    <Composer.Provider service={service} >
      <EveliApp main={Main} secondary={Secondary} toolbar={Toolbar} />
    </Composer.Provider>)
}
