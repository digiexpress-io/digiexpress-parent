import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useLocale } from '@/burger'
import { PublicationsView } from '../frontdesk/views/publications/PublicationsView';

export const Route = createFileRoute('/secured/$locale/worker/publications/')({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (<PublicationsView />)
}
