import React from 'react'
import { UpsertOneFeedback } from '@/eveli-task-feedback';
import { FeedbackProvider } from '@/api-feedback';


export const Route = createFileRoute({
  component: Component,
})

function Component() {
  const { feedbackId } = Route.useParams();
  function handleOnComplete() {
  }
  
  return (<FeedbackProvider><UpsertOneFeedback taskId={feedbackId!} onComplete={handleOnComplete} reload={0} /></FeedbackProvider>)
}
