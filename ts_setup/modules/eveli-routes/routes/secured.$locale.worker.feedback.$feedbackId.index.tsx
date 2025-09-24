import { createFileRoute, useNavigate } from '@tanstack/react-router'
import { useFeedbackBackend } from '@dxs-ts/eveli-api';
import { FeedbackProvider, UpsertOneFeedback } from '@dxs-ts/task-feedback';


export const Route = createFileRoute('/secured/$locale/worker/feedback/$feedbackId/')({
  component: Component,
})

function Component() {
  const { feedbackId } = Route.useParams();
  const backend = useFeedbackBackend();
  const navigate = useNavigate();

  function onFeedbackCancel() {
    navigate({
      from: '/secured/$locale',
      params: { taskId: feedbackId! },
      to: '/secured/$locale/worker/tasks/$taskId'
    });
  }
  function handleOnComplete() {
  }
  
  return (
    <FeedbackProvider backend={backend}>
      <UpsertOneFeedback 
        taskRef={feedbackId!} 
        onComplete={handleOnComplete} 
        onCancel={onFeedbackCancel}
        onDelete={onFeedbackCancel}
        
        reload={0} />
    </FeedbackProvider>)
}
