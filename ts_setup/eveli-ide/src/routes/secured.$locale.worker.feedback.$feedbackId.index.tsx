import React from 'react'
import { useLocale } from '@/api-locale'
import { UpsertOneFeedback } from '@/eveli-task-feedback';
import { FeedbackProvider } from '@/api-feedback';


export const Route = createFileRoute({
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
