import React from 'react';
import { useFeedback, FeedbackApi } from '../feedback-api';
import { CreateOneFeedback } from './CreateOneFeedback';
import { UpdateOneFeedback } from './UpdateOneFeedback';

export interface UpsertOneFeedbackProps {
  taskId: string;
  onComplete: (createdFeedback: FeedbackApi.Feedback) => void;
  viewType: 'FRONTDESK_TASK_VIEW' | 'FEEDBACK_EDITOR_VIEW';
}

export const UpsertOneFeedback: React.FC<UpsertOneFeedbackProps> = (props) => {
  const { findAllFeedback } = useFeedback();
  const [feedbacks, setFeedbacks] = React.useState<FeedbackApi.Feedback[]>();

  React.useEffect(() => {
    findAllFeedback()
      .then(resp => resp)
      .then((resp) => setFeedbacks(resp));
  }, [])

  const feedbackExists = feedbacks?.find(f => f.id === props.taskId);


  if (feedbackExists) {
    return (<UpdateOneFeedback  {...props} />)
  }
  return (<CreateOneFeedback {...props} />);
}