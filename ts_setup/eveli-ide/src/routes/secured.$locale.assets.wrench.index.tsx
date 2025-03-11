import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useLocale, EveliApp } from '@/burger'
import {
  Composer, useWrenchTabChange, useWrenchTabClose,
  WrenchClient, WrenchComponents,
  WrenchRouteSearchParams, parseWrenchSearchParams, LoadTabsFromSearchParams
} from '../wrench';
import { useFetch } from '@dxs-ts/eveli-fetch';

export const Route = createFileRoute('/secured/$locale/assets/wrench/')({
  component: Component,
  validateSearch: (search: Record<string, unknown>): WrenchRouteSearchParams => parseWrenchSearchParams(search)
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();
  const { onTabClose } = useWrenchTabClose();
  const { onTabChange } = useWrenchTabChange();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  const { ast } = useFetch('worker/rest/api/assets/wrench/commands.POST', {})
  const { copy } = useFetch('worker/rest/api/assets/wrench/copyas.POST', {})
  const { getSite } = useFetch('worker/rest/api/assets/wrench/dataModels.GET', {})
  const { debug } = useFetch('worker/rest/api/assets/wrench/debugs.POST', {})
  const { diff } = useFetch('worker/rest/api/assets/wrench/diff.GET', {})
  const { importTag } = useFetch('worker/rest/api/assets/wrench/importTag.POST', {})
  const { createAsset } = useFetch('worker/rest/api/assets/wrench/resources.POST', {})
  const { update } = useFetch('worker/rest/api/assets/wrench/resources.PUT', {})
  const { remove } = useFetch('worker/rest/api/assets/wrench/resources/$id.DELETE', {})
  const { summary } = useFetch('worker/rest/api/assets/wrench/summary/$tagId.GET', {})
  const { version } = useFetch('worker/rest/api/assets/wrench/version.GET', {})


  const service = React.useMemo(() => new WrenchClient.ServiceImpl({
    update, createAsset, ast, getSite, debug, copy, version, diff, summary, remove, importTag,
  }), [update, createAsset, ast, getSite, debug, copy, version, diff, summary, remove, importTag]);
  const { Main, Secondary, Toolbar } = WrenchComponents;
  
  return (
    <Composer.Provider service={service}>
      <EveliApp main={Main} secondary={Secondary} toolbar={Toolbar} onTabClose={onTabClose} onTabChange={onTabChange}>
        <LoadTabsFromSearchParams />
      </EveliApp>
    </Composer.Provider>)
}


/**
 * const ArticleTabIndicator: React.FC<{ entity: HdesApi.Entity<any> }> = ({ entity }) => {
  const theme = useTheme();
  const { isArticleSaved } = WrenchComposerApi.useComposer();
  const saved = isArticleSaved(entity);
  return <span style={{
    paddingLeft: "5px",
    fontSize: '30px',
    color: theme.palette.secondary.light,
    display: saved ? "none" : undefined
  }}>*</span>
}


 */