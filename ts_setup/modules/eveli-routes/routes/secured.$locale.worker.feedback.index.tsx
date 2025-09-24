import { createFileRoute, useNavigate } from '@tanstack/react-router'
import { useFeedbackBackend } from '@dxs-ts/eveli-api'
import { FeedbackAllTasks, FeedbackProvider } from '@dxs-ts/task-feedback';


export const Route = createFileRoute('/secured/$locale/worker/feedback/')({
  component: Component,
})

function Component() {
  const backend = useFeedbackBackend();

  const navigate = useNavigate();
  function handleFeedbackNav(feedbackId: string) {
    navigate({
      from: '/secured/$locale',
      params: { feedbackId },
      to: '/secured/$locale/worker/feedback/$feedbackId'
    });
  }
  
  return (
  <FeedbackProvider backend={backend}>
    <FeedbackAllTasks onOpenFeedback={handleFeedbackNav}/>
  </FeedbackProvider>)
}




