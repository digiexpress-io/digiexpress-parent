import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useLocale, EveliApp } from '@/burger'
import { WrenchClient, WrenchComponents} from '../wrench';
import { Composer } from '../wrench/core/context';
import { useFetch } from '@dxs-ts/eveli-fetch';

export const Route = createFileRoute('/secured/$locale/assets/wrench/')({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();
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
      <EveliApp main={Main} secondary={Secondary} toolbar={Toolbar} />
    </Composer.Provider>)
}
