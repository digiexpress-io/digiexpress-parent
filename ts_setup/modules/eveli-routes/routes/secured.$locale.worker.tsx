import React from 'react'
import { Outlet, createFileRoute, useLocation } from '@tanstack/react-router'
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
  const location = useLocation();

  // IMPORTANT: Let filesystem child route handle its own EveliApp layout
  // This prevents nested EveliApp components which cause:
  // - Duplicate CSS classes with same specificity
  // - Conflicting drawer width settings (parent=300px vs child=450px)
  // - Multiple shell layouts stacking on top of each other
  // Instead of wrapping filesystem in our EveliApp, let it render its own
  if (location.pathname.includes('/filesystem')) {
    return <Outlet />;
  }

  if (mode === 'CONTENT_ONLY') {
    return (<EveliApp contentOnly drawerOpen={false} main={Main} secondary={() => <></>} toolbar={() => <></>} />)
  }

  return (<EveliApp main={Main} secondary={Secondary} toolbar={Toolbar} />)
}


const Main: React.FC<{}> = () => {
  return (<Box p={1}><Outlet /></Box>)
}

export interface SearchParams {
  mode?: 'CONTENT_ONLY',
  explorer?: any[]
}

function parseSearchParams(search: Record<string, unknown>): SearchParams {
  return { mode: search.mode === 'CONTENT_ONLY' ? 'CONTENT_ONLY' : undefined }
}
