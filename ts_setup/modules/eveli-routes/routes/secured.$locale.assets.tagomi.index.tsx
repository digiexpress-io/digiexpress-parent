import React from 'react'
import { createFileRoute } from '@tanstack/react-router'

import { useFetch } from '@dxs-ts/envir-fetch';
import { TagomiComposerApi} from '@dxs-ts/tagomi-api';
import { parseTagomiSearchParams, TagomiRouteSearchParams, TagomiSetup, TagomiStickySave } from '@dxs-ts/tagomi-routes';

import { EveliSetup } from '../eveli-setup';
import { EveliApp } from '../eveli-app';


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

  return (
    <TagomiComposerApi.Provider backend={backend}>
      <EveliApp 
        tabs={TagomiSetup.Tabs} 
        main={TagomiSetup.Main} 
        secondary={TagomiSetup.Secondary} 
        toolbar={MergedToolbar}>

        <TagomiStickySave />
      </EveliApp>
    </TagomiComposerApi.Provider>)
}