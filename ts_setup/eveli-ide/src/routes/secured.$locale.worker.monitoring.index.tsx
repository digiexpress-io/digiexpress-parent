import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useLocale } from '@/burger'
import { ProcessView } from '../frontdesk/views/process/ProcessView';

export const Route = createFileRoute('/secured/$locale/worker/monitoring/')({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (<ProcessView />)
}
