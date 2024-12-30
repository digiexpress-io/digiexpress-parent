
import { UpsertOneFeedback } from '@/feedback';

export const TaskFeedback: React.FC<{ taskId: string, reload: number | undefined }> = ({ taskId, reload }) => {

  function handleFeedbackComplete() {

  }

  return (<UpsertOneFeedback taskId={taskId} onComplete={handleFeedbackComplete} reload={reload ?? 0}/>);
}
