import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useLocale } from '@/api-locale'
import { FeedbackProvider, UpsertOneFeedback } from '../feedback';

export const Route = createFileRoute('/secured/$locale/worker/feedback/$feedbackId/')({
  component: Component,
})

function Component() {
  const { locale, feedbackId } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  function handleOnComplete() {
  }
  
  return (<FeedbackProvider><UpsertOneFeedback taskId={feedbackId!} onComplete={handleOnComplete} reload={0} /></FeedbackProvider>)
}
