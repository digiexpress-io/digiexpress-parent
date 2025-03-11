import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useLocale } from '@/burger'

import {
  Composer, StencilApi,
  StencilClient, Main, Toolbar, Secondary,
  LoadTabsFromSearchParams, StencilRouteSearchParams, parseStencilSearchParams,
  useStencilTabClose, useStencilTabChange
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
  const { onTabClose } = useStencilTabClose();
  const { onTabChange } = useStencilTabChange();

  const { getSite } = useFetch('worker/rest/api/assets/stencil.GET', {});
  const { delete: del } = useFetch('worker/rest/api/assets/stencil/$assetType.DELETE', {});
  const { create } = useFetch('worker/rest/api/assets/stencil/$assetType.POST', {});
  const { update } = useFetch('worker/rest/api/assets/stencil/$assetType.PUT', {});
  const { getReleaseContent } = useFetch('worker/rest/api/assets/stencil/releases/$releaseId.GET', {});
  const { version } = useFetch('worker/rest/api/assets/stencil/version.GET', {});

  React.useLayoutEffect(() => setLocale(locale), [locale])

  const service = React.useMemo(() => {
    const store: StencilApi.StencilRestApi = {getSite, delete: del, create, update, getReleaseContent, version};
    return StencilClient.service({ store });
  }, [getSite, del, create, update, getReleaseContent, version]);

  return (
    <Composer.Provider service={service} >
      <EveliApp main={Main} secondary={Secondary} toolbar={Toolbar} onTabClose={onTabClose} onTabChange={onTabChange}>
        <LoadTabsFromSearchParams />
      </EveliApp>
    </Composer.Provider>)
}

/*
const ArticleTabIndicator: React.FC<{ article: StencilApi.Article, type: StencilComposerApi.NavType }> = ({ article }) => {
  const theme = useTheme();
  const { isArticleSaved } = StencilComposerApi.useComposer();
  const saved = isArticleSaved(article);
  return <span style={{
    paddingLeft: "5px",
    fontSize: '30px',
    color: theme.palette.secondary.light,
    display: saved ? "none" : undefined
  }}>*</span>
}
*/