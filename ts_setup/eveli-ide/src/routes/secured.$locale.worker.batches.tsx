import React from 'react'
import { createFileRoute, Outlet } from '@tanstack/react-router'
import { useLocale } from '@/api-locale';
import { EveliTaskStats } from '@/eveli-task-stats';

export const Route = createFileRoute('/secured/$locale/worker/batches')({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (<Outlet />)
}
