import React from 'react';
import { useFeedback, FeedbackApi } from '../feedback-api';
import { CreateOneFeedback } from './CreateOneFeedback';
import { UpdateOneFeedback } from './UpdateOneFeedback';

export interface UpsertOneFeedbackProps {
  taskId: string;
  onComplete: (createdFeedback: FeedbackApi.Feedback) => void;
}

export const UpsertOneFeedback: React.FC<UpsertOneFeedbackProps> = (props) => {
  const { getOneFeedback } = useFeedback();
  const [feedback, setFeedback] = React.useState<FeedbackApi.Feedback>();

  React.useEffect(() => {
    getOneFeedback(props.taskId)
      .then(resp => resp)
      .then((resp) => setFeedback(resp));
  }, [props.taskId])

  const feedbackExists = feedback ? true : false;
  if (feedbackExists) {
    return (<UpdateOneFeedback  {...props} />)
  }
  return (<CreateOneFeedback {...props} />);
}