import React from 'react'
import { Outlet, createFileRoute } from '@tanstack/react-router'
import { Box } from '@mui/material';

import { Secondary } from '../eveli-setup/Secondary';
import { Toolbar } from '../eveli-setup/Toolbar';
import { EveliApp } from '../eveli-app';


export const Route = createFileRoute('/secured/$locale/worker')({
  component: Component,
  validateSearch: (search: Record<string, unknown>): SearchParams => parseSearchParams(search)
})

function Component() {
  const { mode } = Route.useSearch();

  if (mode === 'CONTENT_ONLY') {
    return (<EveliApp contentOnly drawerOpen={false} main={Main} secondary={() => <></>} toolbar={() => <></>} />)
  }

  return (<EveliApp main={Main} secondary={Secondary} toolbar={Toolbar} />)
}


const Main: React.FC<{}> = () => {
  return (<Box p={1}><Outlet /></Box>)
}

interface SearchParams {
  mode?: 'CONTENT_ONLY',
  explorer?: any[]
}

function parseSearchParams(search: Record<string, unknown>): SearchParams {
  return { mode: search.mode === 'CONTENT_ONLY' ? 'CONTENT_ONLY' : undefined }
}
