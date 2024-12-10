import React from 'react';
import { useFeedback, FeedbackApi } from '../feedback-api';
import { CreateOneFeedback } from './CreateOneFeedback';
import { UpdateOneFeedback } from './UpdateOneFeedback';

export interface UpsertOneFeedbackProps {
  taskId: string;
  onComplete: (upsertedFeedback: FeedbackApi.Feedback) => void;
}

export const UpsertOneFeedback: React.FC<UpsertOneFeedbackProps> = (props) => {
  const { getOneFeedback } = useFeedback();
  const [feedback, setFeedback] = React.useState<FeedbackApi.Feedback>();

  React.useEffect(() => {
    getOneFeedback(props.taskId).then(setFeedback);
  }, [props.taskId])


  function handleOnComplete(upsertedFeedback: FeedbackApi.Feedback) {
    getOneFeedback(props.taskId).then((resp) => {
      setFeedback(resp)
      props.onComplete(upsertedFeedback);
    });
  }

  const ownerState = {...props, onComplete: handleOnComplete};
  const feedbackExists = feedback ? true : false;

  if (feedbackExists) {
    return (<UpdateOneFeedback  {...ownerState} />)
  }
  return (<CreateOneFeedback {...ownerState} />);
}