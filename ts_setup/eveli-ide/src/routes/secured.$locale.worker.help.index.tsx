import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useLocale } from '@/burger'

import { HelpView } from '../frontdesk/views/help/HelpView';

export const Route = createFileRoute('/secured/$locale/worker/help/')({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (<HelpView />)
}
