import React from 'react'
import { createFileRoute, Outlet } from '@tanstack/react-router'
import { useLocale, EveliApp } from '@/burger'


import { Secondary } from '../frontdesk/Secondary';
import { Toolbar } from '../frontdesk/Toolbar';
import { Box } from '@mui/material';


export const Route = createFileRoute('/secured/$locale/worker')({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (<EveliApp main={Main} secondary={Secondary} toolbar={Toolbar} />)
}


const Main: React.FC<{}> = () => {

  return (<Box p={1}>
    <Outlet />
  </Box>)
}