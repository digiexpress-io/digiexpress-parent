import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useLocale } from '@/burger'
import { WorkflowView } from '../frontdesk/views/workflow/WorkflowView';

export const Route = createFileRoute('/secured/$locale/assets/services/')({
  component: Component,
}) 

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();
  React.useLayoutEffect(() => setLocale(locale), [locale])


  return (<WorkflowView />)
}
