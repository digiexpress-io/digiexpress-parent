import React from 'react';
import { Alert, Box, CircularProgress, Divider, TextField, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useIntl } from 'react-intl';

import * as Burger from '@/burger';
import { useFeedback, FeedbackApi } from '../feedback-api';
import { IndicatorPublished } from '../indicator-published';

export interface UpsertOneFeedbackProps {
  taskId: string;
  onComplete: (createdFeedback: FeedbackApi.Feedback) => void;
  viewType: 'FRONTDESK_TASK_VIEW' | 'FEEDBACK_EDITOR_VIEW';
}

export const UpsertOneFeedback: React.FC<UpsertOneFeedbackProps> = ({ taskId, onComplete, viewType }) => {
  const navigate = useNavigate();
  const intl = useIntl();

  const { getOneTemplate, createOneFeedback } = useFeedback();
  const [command, setCommand] = React.useState<FeedbackApi.CreateFeedbackCommand>();
  const [template, setTemplate] = React.useState<FeedbackApi.FeedbackTemplate>();
  const [reply, setReply] = React.useState<string>('');

  console.log(reply)
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
        subLabelValue: template.subLabelValue
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

  function handleCancel() {
    if (template) {
      setReply(template.replys.join("\r\n\r\n"));
    }
    navigate(`/ui/tasks/task/${taskId}`);
  }

  if (!command) {
    return <CircularProgress />
  }

  return (
    <>
      <div style={{ display: 'flex', flexDirection: 'column', padding: 10 }}>

        <Box display='flex' alignItems='center'>
          <Typography variant='h3' fontWeight='bold' mr={3}>Public reply to customer feedback</Typography>
          <IndicatorPublished size='LARGE' />
        </Box>
        <Divider sx={{ my: 2 }} />

        {
          viewType === 'FEEDBACK_EDITOR_VIEW' &&
          <>
            <Typography variant='body2'>{intl.formatMessage({ id: 'feedback.sourceTaskId' })}{': '}{taskId}</Typography>
            <Typography variant='body2'>{intl.formatMessage({ id: 'feedback.formName' })}{': '}{template?.questionnaire.metadata.label}</Typography>
            <Typography variant='body2'>{intl.formatMessage({ id: 'feedback.dateReceived' })}{': '}{template?.questionnaire.metadata.completed}</Typography>
            <Divider sx={{ my: 2 }} />
          </>
        }

        <Typography variant='body2' fontWeight='bold'>{intl.formatMessage({ id: 'feedback.customerFeedback' })}</Typography>
        <Typography variant='body2'>{intl.formatMessage({ id: 'feedback.category' })}{': '}{command.labelValue},</Typography>
        <Typography variant='body2'>{intl.formatMessage({ id: 'feedback.subCategory' })}{': '}{command.subLabelValue}</Typography>
        <Typography mt={2}>{command.content}</Typography>

        <Typography mt={2} fontWeight='bold'>{intl.formatMessage({ id: 'feedback.myReply' })}</Typography>
        {reply ? (
        <TextField onChange={(e) => setReply(e.target.value)}
          sx={{ mb: 3 }}
          multiline
          minRows={4}
          placeholder='Write a reply here'
            value={reply}
          />
        ) : (
          <Typography variant='body2' fontStyle='italic'>
            Before publishing, you must first send an external comment to the customer. Then, you can publish that comment.
          </Typography>)
        }

      </div>
      <Box display='flex' gap={1}>
        <Burger.SecondaryButton onClick={handleCancel} label='button.cancel' />

        {viewType === 'FEEDBACK_EDITOR_VIEW' &&
          <>
            <Burger.SecondaryButton onClick={handleCancel} label='button.delete' />
            <Burger.SecondaryButton onClick={handleCancel} label='button.unpublish' />
          </>
        }
        <Burger.PrimaryButton onClick={handlePublish} label='button.publish' />
      </Box>
    </>
  )
}