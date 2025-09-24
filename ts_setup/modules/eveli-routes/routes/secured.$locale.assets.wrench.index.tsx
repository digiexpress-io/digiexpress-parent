import { createFileRoute } from '@tanstack/react-router'
import React from 'react'

import { useFetch } from '@dxs-ts/envir-fetch';
import { HdesApi, WrenchComposerApi } from '@dxs-ts/wrench-api';
import { WrenchSetup, parseWrenchSearchParams, WrenchRouteSearchParams, WrenchStickySave } from '@dxs-ts/wrench-routes';
import { EveliApp } from '../eveli-app';

import { EveliSetup } from '../eveli-setup';


export const Route = createFileRoute('/secured/$locale/assets/wrench/')({
  component: Component,
  validateSearch: (search: Record<string, unknown>): WrenchRouteSearchParams => parseWrenchSearchParams(search)
})


function Component() {

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
  
  const service = React.useMemo(() => new HdesApi.ServiceImpl({
    update, createAsset, ast, getSite, debug, copy, diff, summary, remove, importTag,
  }), [update, createAsset, ast, getSite, debug, copy, diff, summary, remove, importTag]);
  const { Main, Secondary, Tabs } = WrenchSetup;
  
  return (
    <WrenchComposerApi.Provider service={service}>
      <EveliApp main={Main} secondary={Secondary} toolbar={EveliSetup.Toolbar} tabs={Tabs}>
        <WrenchStickySave />
      </EveliApp>
    </WrenchComposerApi.Provider>)
}