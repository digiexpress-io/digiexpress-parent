import React from 'react';
import { FormattedMessage } from 'react-intl';
import { useFeedback, FeedbackApi } from '../api-feedback';
import { CreateOneFeedback } from './CreateOneFeedback';
import { UpdateOneFeedback } from './UpdateOneFeedback';
import { useSnackbar } from 'notistack';

export interface UpsertOneFeedbackProps {
  taskRef: string;
  reload: number;
  onComplete: (upsertedFeedback: FeedbackApi.Feedback) => void;
  onDelete: () => void;
  onCancel: () => void;
  allowDelete?: boolean;
}

export const UpsertOneFeedback: React.FC<UpsertOneFeedbackProps> = (props) => {
  const { getOneFeedback, isTaskFeedbackEnabled } = useFeedback();
  const { enqueueSnackbar } = useSnackbar();
  const [feedback, setFeedback] = React.useState<FeedbackApi.Feedback>();
  const [enabled, setEnabled] = React.useState<true | false | undefined>();

  React.useEffect(() => {
    setEnabled(undefined);

    isTaskFeedbackEnabled(props.taskRef).then((enabled) => {
      if(enabled) {
        getOneFeedback(props.taskRef).then(setFeedback)
      }
      setEnabled(enabled);
    });
  }, [props.taskRef, props.reload])

  function handleOnComplete(upsertedFeedback: FeedbackApi.Feedback) {
    getOneFeedback(props.taskRef).then((resp) => {
      setFeedback(resp)
      enqueueSnackbar(<FormattedMessage id="task.feedback.publishedSaved" />, { variant: 'success' });
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
    return (<UpdateOneFeedback {...ownerState} allowDelete={props.allowDelete} taskId={props.taskRef} />)
  }  
  return (<CreateOneFeedback {...ownerState} />);
}