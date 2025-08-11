import { createFileRoute } from '@tanstack/react-router'
import { FeedbackProvider } from '@dxs-ts/eveli-api';
import { UpsertOneFeedback } from '../eveli-task-feedback';


export const Route = createFileRoute('/secured/$locale/worker/feedback/$feedbackId/')({
  component: Component,
})

function Component() {
  const { feedbackId } = Route.useParams();
  function handleOnComplete() {
  }
  
  return (<FeedbackProvider><UpsertOneFeedback taskRef={feedbackId!} onComplete={handleOnComplete} reload={0} /></FeedbackProvider>)
}
