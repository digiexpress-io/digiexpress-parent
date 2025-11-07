import React from 'react'
import { createFileRoute } from '@tanstack/react-router'

import { useFetch } from '@dxs-ts/envir-fetch';
import { parseTagomiSearchParams, TagomiRouteSearchParams } from '@dxs-ts/tagomi-routes';

import { EveliSetup } from '../eveli-setup';
import { EveliApp } from '../eveli-app';

import { ContractDashboard } from '../../contract-composer';


export const Route = createFileRoute('/secured/$locale/contracts/')({
  component: Component,
  validateSearch: (search: Record<string, unknown>): TagomiRouteSearchParams => parseTagomiSearchParams(search)
})


const MergedToolbar: React.FC = () => {
  return (<EveliSetup.Toolbar />)
}

const dummySecondary: React.FC = () => {
  return (<>MENU ITEMS</>)
}

function Component() {
  const { backend } = useFetch('worker/rest/api/assets/tagomi.GET', {});


  return (
    <EveliApp
      main={ContractDashboard}
      secondary={dummySecondary}
      toolbar={MergedToolbar}
    />
  )


}