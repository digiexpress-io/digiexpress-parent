import React from 'react';
import { Box, CircularProgress, Divider, TextField, Typography, useTheme, Button, Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material';
import { useNavigate } from '@tanstack/react-router';
import { useIntl, FormattedMessage } from 'react-intl';

import { useFeedback, FeedbackApi } from '../../api-feedback';
import { StatusIndicator } from '../status-indicator';
import { ApprovalCount } from '../approval-count';
import { EveliDateTimeFormatter } from '@/eveli-datetime-formatter';
import { FeedbackContent } from './FeedbackContent';
import { CancelButton } from '@/eveli-styles';

export interface UpdateOneFeedbackProps {
  taskRef: string;
  taskId: string;
  onComplete: (createdFeedback: FeedbackApi.Feedback) => void;
  allowDelete?: boolean;
}

export const UpdateOneFeedback: React.FC<UpdateOneFeedbackProps> = ({ taskRef, taskId, onComplete, allowDelete = true }) => {
  const navigate = useNavigate();
  const intl = useIntl();
  const theme = useTheme();

  const { modifyOneFeedback, getOneFeedback, deleteOneFeedback } = useFeedback();
  const [feedback, setFeedback] = React.useState<FeedbackApi.Feedback>();
  const [reply, setReply] = React.useState<string>('');
  const [customerTitle, setCustomerTitle] = React.useState<string>('');
  const [question, setQuestion] = React.useState<string>('');
  const [confirmOpen, setConfirmOpen] = React.useState(false);
  const [savedReply, setSavedReply] = React.useState<string>('');
  const [main, setMain] = React.useState({ labelKey: '', labelValue: '' });
  const [sub, setSub] = React.useState({ subLabelKey: '', subLabelValue: '' });


  React.useEffect(() => {
    getOneFeedback(taskRef)
      .then((resp) => {
        setFeedback(resp);
        setReply(resp?.replyText ?? '');
        setQuestion(resp?.content?.question ?? '');
        setSavedReply(resp?.replyText ?? '');

        setMain({ labelKey: resp?.labelKey ?? '', labelValue: resp?.labelValue ?? '' })
        setSub({ subLabelKey: resp?.subLabelKey ?? '', subLabelValue: resp?.subLabelValue ?? '' })

        setCustomerTitle(resp?.customerTitle ?? '')
      });
  }, []);

  function handlePublish() {
    if (!feedback) {
      return;
    }

    const command: FeedbackApi.ModifyOneFeedbackReplyCommand = {
      id: feedback.id,
      commandType: 'MODIFY_ONE_FEEDBACK_REPLY',
      reply: reply,
      question: question,

      labelKey: main.labelKey,
      labelValue: main.labelValue,

      subLabelKey: sub.subLabelKey,
      subLabelValue: sub.subLabelValue,

      customerTitle: customerTitle
    };

    modifyOneFeedback(taskRef, command).then(updatedFeedback => {
      onComplete(updatedFeedback);
      setSavedReply(reply);
    });
  }

  function confirmDelete() {
    deleteOneFeedback(taskRef).then(feedback => {
      onComplete(feedback);
    });
    navigate({
      from: '/secured/$locale',
      params: { taskId: taskRef },
      to: '/secured/$locale/worker/tasks/$taskId'
    });
  }

  if (!feedback) {
    return (<CircularProgress />)
  }


  return (
    <div style={{ display: 'flex', flexDirection: 'column', padding: theme.spacing(3) }}>
      <Box display='flex' alignItems='center'>
        <Typography variant='h3' fontWeight='bold' mr={1}>
          {intl.formatMessage({ id: 'feedback.update.title' })}
        </Typography>
        <StatusIndicator size='LARGE' taskId={taskId} />
        <Box flexGrow={1} />
        <ApprovalCount approvalCount={feedback.thumbsUpCount} disapprovalCount={feedback.thumbsDownCount} />
      </Box>


      <Typography variant='body2'>
        <Box component='span' fontWeight='bold'>
          {intl.formatMessage({ id: 'feedback.taskReferenceId' })}
          {intl.formatMessage({ id: 'eveli.textSeparatorColon' })}
        </Box>
        {taskRef}
      </Typography>

      <Typography variant='body2'>
        <Box component='span' fontWeight='bold'>
          {intl.formatMessage({ id: 'feedback.updated' })}
          {intl.formatMessage({ id: 'eveli.textSeparatorColon' })}
        </Box>
        <EveliDateTimeFormatter value={feedback.updatedOnDate} variant='text' />
      </Typography>

      <Typography variant='body2'>
        <Box component='span' fontWeight='bold'>
          {intl.formatMessage({ id: 'feedback.updatedBy' })}
          {intl.formatMessage({ id: 'eveli.textSeparatorColon' })}
        </Box>
        {feedback.updatedBy}
      </Typography>

      <Divider sx={{ my: 2 }} />

      <FeedbackContent feedback={{ ...feedback, ...main, ...sub, customerTitle }} onChange={(props) => {
        setMain({
          labelKey: props.labelKey,
          labelValue: props.labelValue,
        })
        setSub({
          subLabelKey: props.subLabelKey ?? '',
          subLabelValue: props.subLabelValue ?? '',
        }),
          setCustomerTitle(props.customerTitle)
      }} />

      <Typography fontWeight='bold'>{intl.formatMessage({ id: 'feedback.feedbackValue' })}</Typography>
      <TextField onChange={(e) => setQuestion(e.target.value)}
        sx={{ mb: 3 }}
        multiline
        minRows={4}
        placeholder={intl.formatMessage({ id: 'feedback.feedbackValue.placeholder', defaultMessage: 'Customer question' })}
        value={question}
      />


      <Typography mt={2} fontWeight='bold'>{intl.formatMessage({ id: 'feedback.myReply' })}</Typography>
      <TextField onChange={(e) => setReply(e.target.value)}
        sx={{ mb: 3 }}
        multiline
        minRows={4}
        placeholder='Write a reply here'
        value={reply}
      />

      <Box display='flex' gap={1}>
        {allowDelete && (
          <>
            <Button onClick={() => setConfirmOpen(true)} variant='text'>
              <FormattedMessage id='button.delete' />
            </Button>
          </>
        )}
        <CancelButton
          onClick={() => setReply(savedReply)}
          disabled={reply === savedReply}
        />
        <Button
          variant='contained'
          onClick={handlePublish}
          disabled={reply === savedReply}
        >
          <FormattedMessage id='button.update' />
        </Button>
      </Box>

      {allowDelete && (
        <Dialog
          open={confirmOpen}
          onClose={() => setConfirmOpen(false)}
          maxWidth="xs"
        >
          <DialogTitle>
            <FormattedMessage id='feedback.delete.confirmTitle' />
          </DialogTitle>
          <DialogContent>
            <Typography>
              <FormattedMessage id='feedback.delete.confirmText' />
            </Typography>
          </DialogContent>
          <DialogActions>
            <CancelButton onClick={() => setConfirmOpen(false)} />
            <Button onClick={confirmDelete} color='error'>
              <FormattedMessage id='button.confirmDelete' />
            </Button>
          </DialogActions>
        </Dialog>
      )}

    </div>
  )
}