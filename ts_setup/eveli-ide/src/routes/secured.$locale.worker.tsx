import React from 'react'
import { createFileRoute, Outlet } from '@tanstack/react-router'



import { Secondary } from '../eveli-setup/Secondary';
import { Toolbar } from '../eveli-setup/Toolbar';
import { Box } from '@mui/material';
import { useLocale } from '@/api-locale';
import { EveliApp } from '@/eveli-app';


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