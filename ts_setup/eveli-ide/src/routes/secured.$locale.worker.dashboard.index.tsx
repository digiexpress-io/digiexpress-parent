import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useLocale, EveliTaskStats } from '@/burger'

export const Route = createFileRoute('/secured/$locale/worker/dashboard/')({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (<EveliTaskStats />)
}
