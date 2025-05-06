import React from 'react';
import { Box, CircularProgress, Divider, TextField, Typography, useTheme, Button, Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material';
import { useNavigate } from '@tanstack/react-router';
import { useIntl, FormattedMessage } from 'react-intl';

import { useFeedback, FeedbackApi } from '../../api-feedback';
import { StatusIndicator } from '../status-indicator';
import { ApprovalCount } from '../approval-count';
import { EveliDateTimeFormatter } from '@/eveli-datetime-formatter';
import { FeedbackContent } from './FeedbackContent';

export interface UpdateOneFeedbackProps {
  taskId: string;
  onComplete: (createdFeedback: FeedbackApi.Feedback) => void;
}

export const UpdateOneFeedback: React.FC<UpdateOneFeedbackProps> = ({ taskId, onComplete }) => {
  const navigate = useNavigate();
  const intl = useIntl();
  const theme = useTheme();

  const { modifyOneFeedback, getOneFeedback, deleteOneFeedback } = useFeedback();

  const [feedback, setFeedback] = React.useState<FeedbackApi.Feedback>();
  const [reply, setReply] = React.useState<string>('');

  const [confirmOpen, setConfirmOpen] = React.useState(false);

  const [savedReply, setSavedReply] = React.useState<string>('');

  React.useEffect(() => {
    getOneFeedback(taskId)
      .then((resp) => {
        setFeedback(resp);
        setReply(resp?.replyText ?? '');
        setSavedReply(resp?.replyText ?? '');
      });
  }, []);  

  function handlePublish() {
    if (!feedback) {
      return;
    }
  
    const command: FeedbackApi.ModifyOneFeedbackReplyCommand = {
      id: feedback.id,
      commandType: 'MODIFY_ONE_FEEDBACK_REPLY',
      reply: reply
    };
  
    modifyOneFeedback(taskId, command).then(updatedFeedback => {
      onComplete(updatedFeedback);
      setSavedReply(reply);
    });
  }
  

  function confirmDelete() {
    deleteOneFeedback(taskId).then(feedback => {
      onComplete(feedback);
    });
    navigate({
      from: '/secured/$locale',
      params: { taskId },
      to: '/secured/$locale/worker/tasks/$taskId'
    });
  }

  if (!feedback) {
    return (<CircularProgress />)
  }
  

  return (
    <div style={{ display: 'flex', flexDirection: 'column', padding: theme.spacing(3) }}>
      <Box display='flex' alignItems='center'>
        <Typography variant='h3' fontWeight='bold' mr={1}>{intl.formatMessage({ id: 'feedback.update.title' })}</Typography>
        <StatusIndicator size='LARGE' taskId={taskId} />
        <Box flexGrow={1} />
        <ApprovalCount approvalCount={feedback.thumbsUpCount} disapprovalCount={feedback.thumbsDownCount} />
      </Box>
      <Divider sx={{ my: 2 }} />
      
      <Typography variant='body2'>
        <Box component='span' fontWeight='bold'>
          {intl.formatMessage({ id: 'feedback.updated' })}:
        </Box>{' '}
        <EveliDateTimeFormatter value={feedback.updatedOnDate} variant='text' />
      </Typography>
      <Typography variant='body2'>
        <Box component='span' fontWeight='bold'>
          {intl.formatMessage({ id: 'feedback.updatedBy' })}:
        </Box>{' '}
        {feedback.updatedBy}
      </Typography>

      <Divider sx={{ my: 2 }} />

      <FeedbackContent feedback={feedback.content} />

      <Typography mt={2} fontWeight='bold'>{intl.formatMessage({ id: 'feedback.myReply' })}</Typography>

        <TextField onChange={(e) => setReply(e.target.value)}
          sx={{ mb: 3 }}
          multiline
          minRows={4}
          placeholder='Write a reply here'
          value={reply}
      />

      <Box display='flex' gap={1}>
        <Button onClick={() => setConfirmOpen(true)} variant='text'>
          <FormattedMessage id='button.delete' />
        </Button>
        <Button
          variant='outlined'
          onClick={() => setReply(savedReply)}
          disabled={reply === savedReply}
        >
          <FormattedMessage id='button.cancel' />
        </Button>
        <Button
          variant='contained'
          onClick={handlePublish}
          disabled={reply === savedReply}
        >
          <FormattedMessage id='button.update' />
        </Button>
      </Box>

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
          <Button onClick={() => setConfirmOpen(false)}>
            <FormattedMessage id='button.cancel' />
          </Button>
          <Button onClick={confirmDelete} color='error'>
            <FormattedMessage id='button.confirmDelete' />
          </Button>
        </DialogActions>
      </Dialog>

    </div>
  )
}