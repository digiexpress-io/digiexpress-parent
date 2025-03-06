import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useLocale } from '@/burger'
import { FeedbackAllTasks, FeedbackProvider } from '../feedback';


export const Route = createFileRoute('/secured/$locale/worker/feedback/')({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (<FeedbackProvider><FeedbackAllTasks /></FeedbackProvider>)
}
