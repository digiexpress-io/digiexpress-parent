import React from 'react';
import { Box, CircularProgress, Divider, TextField, Typography, useTheme } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useIntl } from 'react-intl';
import ReactMarkdown from 'react-markdown';

import * as Burger from '@/burger';
import { useFeedback, FeedbackApi } from '../feedback-api';
import { StatusIndicator } from '../status-indicator';
import { ApprovalCount } from '../approval-count';

export interface UpdateOneFeedbackProps {
  taskId: string;
  onComplete: (createdFeedback: FeedbackApi.Feedback) => void;
}

export const UpdateOneFeedback: React.FC<UpdateOneFeedbackProps> = ({ taskId, onComplete }) => {
  const navigate = useNavigate();
  const intl = useIntl();
  const theme = useTheme();

  const { getOneTemplate, createOneFeedback, findAllFeedback, deleteOneFeedback } = useFeedback();

  const [feedbacks, setFeedbacks] = React.useState<FeedbackApi.Feedback[]>();
  const [command, setCommand] = React.useState<FeedbackApi.CreateFeedbackCommand>();
  const [template, setTemplate] = React.useState<FeedbackApi.FeedbackTemplate>();
  const [reply, setReply] = React.useState<string>('');

  React.useEffect(() => {
    findAllFeedback()
      .then(resp => resp)
      .then((resp) => setFeedbacks(resp));
  }, [])

  const feedback = feedbacks?.find(f => f.sourceId === taskId);


  React.useEffect(() => {
    getOneTemplate(taskId!).then(template => {

      setCommand({
        content: template.content,
        labelKey: template.labelKey,
        labelValue: template.labelValue,
        locale: template.locale,
        origin: template.origin,
        processId: template.processId,
        userId: template.userId,
        subLabelKey: template.subLabelKey,
        subLabelValue: template.subLabelValue,
        reply
      });

      setTemplate(template);
      setReply(template.replys.join("\r\n\r\n"));
    });

  }, []);

  function handlePublish() {
    if (command) {
      createOneFeedback(taskId, command).then(feedback => {
        onComplete(feedback);
      });
    }
  }

  function handleDelete() {
    deleteOneFeedback(taskId).then(feedback => {
      onComplete(feedback);
    });
    navigate(`/ui/tasks/task/${taskId}`);
  }

  if (!command || !feedback) {
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
      <Typography variant='body2'>{intl.formatMessage({ id: 'feedback.dateReceived' })}{': '}<Burger.DateTimeFormatter timestamp={template?.questionnaire.metadata.completed} /></Typography>
      <Typography variant='body2'>{intl.formatMessage({ id: 'feedback.updated' })}{': '}<Burger.DateTimeFormatter timestamp={feedback.updatedOnDate} /></Typography>
      <Typography variant='body2'>{intl.formatMessage({ id: 'feedback.updatedBy' })}{': '}{feedback.updatedBy}</Typography>
      <Divider sx={{ my: 2 }} />
      <Typography variant='body2' fontWeight='bold'>{intl.formatMessage({ id: 'feedback.customerFeedback' })}</Typography>
      <Typography variant='body2'>{intl.formatMessage({ id: 'feedback.category' })}{': '}{feedback.labelValue}</Typography>
      <Typography variant='body2'>{intl.formatMessage({ id: 'feedback.subCategory' })}{': '}{feedback.subLabelValue}</Typography>
      <Box component='span' mt={2}><ReactMarkdown>{template?.content}</ReactMarkdown></Box>

      <Typography mt={2} fontWeight='bold'>{intl.formatMessage({ id: 'feedback.myReply' })}</Typography>
      {reply ? (
        <TextField onChange={(e) => setReply(e.target.value)}
          sx={{ mb: 3 }}
          multiline
          minRows={4}
          placeholder='Write a reply here'
          value={reply}
        />
      ) : (<Box p={2}>
        <Typography variant='body2' fontStyle='italic'>{intl.formatMessage({ id: 'feedback.noFeedback.info1' })}</Typography>
        <Typography variant='body2' fontStyle='italic'>{intl.formatMessage({ id: 'feedback.noFeedback.info2' })}</Typography>
      </Box>
      )
      }
      <Box display='flex' gap={1}>
        <Burger.SecondaryButton onClick={handleDelete} label='button.delete' />
        <Burger.PrimaryButton onClick={handlePublish} label='button.update' />
      </Box>
    </div>
  )
}