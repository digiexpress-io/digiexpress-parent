import React from 'react';
import { Box, CircularProgress, Divider, TextField, Typography, useTheme, Button, Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material';
import { useIntl, FormattedMessage } from 'react-intl';
import { DateTimeFormatter } from '@dxs-ts/xui-datetime';

import { useFeedback, FeedbackApi } from '../api-feedback';
import { StatusIndicator } from '../status-indicator';
import { ApprovalCount } from '../approval-count';
import { FeedbackContent } from './FeedbackContent';
import { UpsertOneFeedbackRoot, useUtilityClasses } from './useUtilityClasses';

export interface UpdateOneFeedbackProps {
  taskRef: string;
  taskId: string;
  onComplete: (createdFeedback: FeedbackApi.Feedback) => void;
  onDelete: () => void;
  
  allowDelete?: boolean;
  slots?: {
    AcceptButton: React.ElementType<{ disabled: boolean, onClick: () => Promise<void> }>;
    CancelButton: React.ElementType<{  disabled: boolean, onClick: () => Promise<void> }>;
  }
}

export const UpdateOneFeedback: React.FC<UpdateOneFeedbackProps> = ({ slots, taskRef, taskId, onComplete, onDelete, allowDelete = true }) => {
  const intl = useIntl();
  const theme = useTheme();
  const classes = useUtilityClasses();

  const { modifyOneFeedback, getOneFeedback, deleteOneFeedback } = useFeedback();
  const [feedback, setFeedback] = React.useState<FeedbackApi.Feedback>();
  const [reply, setReply] = React.useState<string>('');
  const [customerTitle, setCustomerTitle] = React.useState<string>('');
  const [question, setQuestion] = React.useState<string>('');
  const [confirmOpen, setConfirmOpen] = React.useState(false);
  const [savedReply, setSavedReply] = React.useState<string>('');
  const [main, setMain] = React.useState({ labelKey: '', labelValue: '' });

  const mapToLocalState = React.useCallback((resp: FeedbackApi.Feedback | undefined) => {
    setFeedback(resp);
    setReply(resp?.replyText ?? '');
    setQuestion(resp?.content?.question ?? '');
    setSavedReply(resp?.replyText ?? '');
    setMain({ labelKey: resp?.labelKey ?? '', labelValue: resp?.labelValue ?? '' });
    setCustomerTitle(resp?.customerTitle ?? '');
  }, []);

  React.useEffect(() => {
    getOneFeedback(taskRef).then(mapToLocalState);
  }, [getOneFeedback, taskRef, mapToLocalState]);

  const handlePublish = React.useCallback(async function() {
    if (!feedback) {
      return;
    }

    const command: FeedbackApi.ModifyOneFeedbackReplyCommand = {
      id: feedback.id,
      commandType: 'MODIFY_ONE_FEEDBACK_REPLY',
      reply,
      question,
      labelKey: main.labelKey,
      labelValue: main.labelValue,
      customerTitle
    };

    return modifyOneFeedback(taskRef, command).then(updatedFeedback => {
      onComplete(updatedFeedback);
      setSavedReply(reply);
      mapToLocalState(updatedFeedback);
    });
  }, [main, customerTitle, taskRef, reply, question, feedback?.id, modifyOneFeedback, onComplete, mapToLocalState]);

  function confirmDelete() {
    deleteOneFeedback(taskRef).then(fb => {
      onComplete(fb);
    });
    onDelete();
  }

  if (!feedback) {
    return (<CircularProgress />)
  }

  const isChanged = (
    reply !== savedReply ||
    question !== feedback.content?.question ||
    customerTitle !== feedback?.customerTitle ||
    main.labelKey !== feedback?.labelKey ||
    main.labelValue !== feedback?.labelValue
  );

  const AcceptButton: React.ElementType<{ disabled: boolean, onClick: () => Promise<void> }> = slots?.AcceptButton ?? SaveFeedback;
  const CancelButton: React.ElementType<{ disabled: boolean, onClick: () => Promise<void> }> =  slots?.CancelButton ?? CancelFeedback;

  return (
    <UpsertOneFeedbackRoot className={classes.root}>
      <div className={classes.container}>
        <Box display='flex' alignItems='center'>
          <Typography variant='h3' className={classes.title} mr={1}>
            {intl.formatMessage({ id: 'feedback.update.title' })}
          </Typography>
          <StatusIndicator size='LARGE' taskId={taskId} />
          <Box flexGrow={1} />
          <ApprovalCount approvalCount={feedback.thumbsUpCount} disapprovalCount={feedback.thumbsDownCount} />
        </Box>

        <div className={classes.section}>
          <Typography variant='body2'>
            <Box component='span' className={classes.boldLabel}>
              {intl.formatMessage({ id: 'feedback.taskReferenceId' })}
              {": "}
            </Box>
            {taskRef}
          </Typography>

          <Typography variant='body2'>
            <Box component='span' className={classes.boldLabel}>
              {intl.formatMessage({ id: 'feedback.updated' })}
              {": "}
            </Box>
            <DateTimeFormatter value={feedback.updatedOnDate} variant='text' />
          </Typography>

          <Typography variant='body2'>
            <Box component='span' className={classes.boldLabel}>
              {intl.formatMessage({ id: 'feedback.updatedBy' })}
              {": "}
            </Box>
            {feedback.updatedBy}
          </Typography> 
        </div>

        <Divider />

        <FeedbackContent
          feedback={{ ...feedback, ...main, customerTitle }}
          onChange={(props) => {
            setMain({ labelKey: props.labelKey, labelValue: props.labelValue });
            setCustomerTitle(props.customerTitle);
          }}
        />

        <div className={classes.section}>
          <Typography className={classes.boldLabel}>
            {intl.formatMessage({ id: 'feedback.feedbackValue' })}
          </Typography>
          <TextField
            className={classes.field}
            onChange={(e) => setQuestion(e.target.value)}
            multiline
            minRows={4}
            placeholder={intl.formatMessage({ id: 'feedback.feedbackValue.placeholder' })}
            value={question}
          />
        </div>

        <div className={classes.section}>
          <Typography className={classes.boldLabel}>
            {intl.formatMessage({ id: 'feedback.myReply' })}
          </Typography>
          <TextField
            className={classes.field}
            onChange={(e) => setReply(e.target.value)}
            multiline
            minRows={4}
            placeholder={intl.formatMessage({ id: 'feedback.myReply.placeholder' })}
            value={reply}
          />
        </div>

        <Box className={classes.actions}>
          <CancelButton onClick={async () => {}} disabled={false} />
          <AcceptButton onClick={handlePublish} disabled={!isChanged}/>
        </Box>

        {allowDelete && (
          <>
            <Divider />
            <Box className={classes.actions}>
              <Button color='error' variant='outlined' onClick={() => setConfirmOpen(true)}>
                <FormattedMessage id='button.delete' />
              </Button>
            </Box>

            <Dialog open={confirmOpen} onClose={() => setConfirmOpen(false)} maxWidth="xs">
              <DialogTitle><FormattedMessage id='feedback.delete.confirmTitle' /></DialogTitle>
              <DialogContent>
                <Typography color="error">
                  <FormattedMessage id='feedback.delete.confirmText' />
                </Typography>
              </DialogContent>
              <DialogActions>
                <Button onClick={() => setConfirmOpen(false)} variant="outlined">
                  <FormattedMessage id='button.cancel' />
                </Button>
                <Button onClick={confirmDelete} color="error">
                  <FormattedMessage id='button.confirmDelete' />
                </Button>
              </DialogActions>
            </Dialog>
          </>
        )}
      </div>
    </UpsertOneFeedbackRoot>
  );
}

const SaveFeedback: React.FC<{ disabled: boolean, onClick: () => Promise<void>  }> = ({ disabled, onClick }) => {
  return (
    <Button variant='contained' disabled={disabled} onClick={onClick}><FormattedMessage id='button.save' /></Button>
  )
}

const CancelFeedback: React.FC<{ disabled: boolean, onClick: () => Promise<void>  }> = ({ disabled, onClick }) => {
  return (
    <Button variant="outlined" disabled={disabled} onClick={onClick}><FormattedMessage id='button.cancel' /></Button>
  )
}
