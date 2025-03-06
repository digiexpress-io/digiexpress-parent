import React from 'react'
import { createFileRoute, Outlet } from '@tanstack/react-router'
import { useLocale, EveliApp } from '@/burger'


import { Secondary } from '../frontdesk/Secondary';
import { Toolbar } from '../frontdesk/Toolbar';


export const Route = createFileRoute('/secured/$locale/worker')({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (<EveliApp main={Outlet} secondary={Secondary} toolbar={Toolbar} />)
}
