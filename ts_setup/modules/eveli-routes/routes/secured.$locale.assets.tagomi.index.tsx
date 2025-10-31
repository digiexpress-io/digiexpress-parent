import React from 'react'
import { createFileRoute } from '@tanstack/react-router'

import { useFetch } from '@dxs-ts/envir-fetch';
import { TagomiComposerApi} from '@dxs-ts/tagomi-api';
import { parseTagomiSearchParams, TagomiRouteSearchParams, TagomiSetup, TagomiStickySave } from '@dxs-ts/tagomi-routes';

import { EveliSetup } from '../eveli-setup';
import { EveliApp } from '../eveli-app';
import { HdesApi, WrenchComposerApi } from '@dxs-ts/wrench-api';


export const Route = createFileRoute('/secured/$locale/assets/tagomi/')({
  component: Component,
  validateSearch: (search: Record<string, unknown>): TagomiRouteSearchParams => parseTagomiSearchParams(search)
})


const MergedToolbar: React.FC = () => {
  return <>
    <EveliSetup.Toolbar />

  </>
}

function Component() {
  const { backend } = useFetch('worker/rest/api/assets/tagomi.GET', {});
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

  
  return (
    <WrenchComposerApi.Provider service={service}>
      <TagomiComposerApi.Provider backend={backend}>
        <EveliApp 
          tabs={TagomiSetup.Tabs} 
          main={TagomiSetup.Main} 
          secondary={TagomiSetup.Secondary} 
          toolbar={MergedToolbar}>

          <TagomiStickySave />
        </EveliApp>
      </TagomiComposerApi.Provider>
    </WrenchComposerApi.Provider>)
}