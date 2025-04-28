import React from 'react';
import { Box, CircularProgress, Divider, TextField, Typography, useTheme, Button } from '@mui/material';
import { useNavigate } from '@tanstack/react-router';
import { useIntl, FormattedMessage } from 'react-intl';
import ReactMarkdown from 'react-markdown';

import { useFeedback, FeedbackApi } from '../../api-feedback';
import { StatusIndicator } from '../status-indicator';
import { ApprovalCount } from '../approval-count';
import { EveliDateTimeFormatter } from '@/eveli-datetime-formatter';

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

  React.useEffect(() => {
    getOneFeedback(taskId)
      .then(resp => resp)
      .then((resp) => {


        setFeedback(resp);

        setReply(resp?.replyText ?? '');
      });
  }, [])


  function handlePublish() {
    if (!feedback) {
      return;
    }

    const command: FeedbackApi.ModifyOneFeedbackReplyCommand = {
      id: feedback.id,
      commandType: 'MODIFY_ONE_FEEDBACK_REPLY',
      reply: reply
    };
    modifyOneFeedback(taskId, command).then(onComplete);

  }

  function handleDelete() {
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
      <Typography variant='body2' fontWeight='bold'>{intl.formatMessage({ id: 'feedback.customerFeedback' })}</Typography>
      <Typography variant='body2'>{intl.formatMessage({ id: 'feedback.category' })}{': '}{feedback.labelValue}</Typography>
      <Typography variant='body2'>{intl.formatMessage({ id: 'feedback.subCategory' })}{': '}{feedback.subLabelValue}</Typography>
      <Box component='span' mt={2}><ReactMarkdown>{feedback.content}</ReactMarkdown></Box>

      <Typography mt={2} fontWeight='bold'>{intl.formatMessage({ id: 'feedback.myReply' })}</Typography>

        <TextField onChange={(e) => setReply(e.target.value)}
          sx={{ mb: 3 }}
          multiline
          minRows={4}
          placeholder='Write a reply here'
          value={reply}
        />

      <Box display='flex' gap={1}>
        <Button onClick={handleDelete}  variant='text'><FormattedMessage id='button.delete'/></Button>
        <Button variant='contained' onClick={handlePublish}><FormattedMessage id='button.update'/></Button>
      </Box>
    </div>
  )
}