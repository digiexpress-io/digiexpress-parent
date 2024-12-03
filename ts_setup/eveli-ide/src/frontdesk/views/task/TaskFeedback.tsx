
import { UpsertOneFeedback } from '@/feedback';

export const TaskFeedback: React.FC<{ taskId: string }> = ({ taskId }) => {

  function handleFeedbackComplete() {

  }

  return (<UpsertOneFeedback taskId={taskId} onComplete={handleFeedbackComplete} />);
}
