import React from 'react';
import { FormattedMessage } from 'react-intl';
import { useFeedback, FeedbackApi } from '../feedback-api';
import { CreateOneFeedback } from './CreateOneFeedback';
import { UpdateOneFeedback } from './UpdateOneFeedback';

export interface UpsertOneFeedbackProps {
  taskId: string;
  reload: number;
  onComplete: (upsertedFeedback: FeedbackApi.Feedback) => void;
}

export const UpsertOneFeedback: React.FC<UpsertOneFeedbackProps> = (props) => {
  const { getOneFeedback, isTaskFeedbackEnabled } = useFeedback();
  const [feedback, setFeedback] = React.useState<FeedbackApi.Feedback>();
  const [enabled, setEnabled] = React.useState<true | false | undefined>();

  React.useEffect(() => {
    setEnabled(undefined);

    isTaskFeedbackEnabled(props.taskId).then((enabled) => {

      if(enabled) {
        getOneFeedback(props.taskId).then(setFeedback)
      }
      setEnabled(enabled);
    });
  }, [props.taskId, props.reload])


  function handleOnComplete(upsertedFeedback: FeedbackApi.Feedback) {
    getOneFeedback(props.taskId).then((resp) => {
      setFeedback(resp)
      props.onComplete(upsertedFeedback);
    });
  }

  const ownerState = {...props, onComplete: handleOnComplete, enabled, version: props.reload};
  const feedbackExists = feedback ? true : false;

  if(ownerState.enabled === undefined) {
    return <>...loading</>
  }


  if(ownerState.enabled === false) {
    return <FormattedMessage id='feedback.notenabled'/>
  }

  if (feedbackExists) {
    return (<UpdateOneFeedback  {...ownerState} />)
  }
  return (<CreateOneFeedback {...ownerState} />);
}