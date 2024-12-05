import React from 'react';
import { Box, CircularProgress, Divider, TextField, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import ReactMarkdown from 'react-markdown';
import { useNavigate } from 'react-router-dom';

import * as Burger from '@/burger';
import { useFeedback, FeedbackApi } from '../feedback-api';
import { StatusIndicator } from '../status-indicator';

export interface CreateOneFeedbackProps {
  taskId: string;
  onComplete: (createdFeedback: FeedbackApi.Feedback) => void;
  viewType: 'FRONTDESK_TASK_VIEW' | 'FEEDBACK_EDITOR_VIEW';
}

export const CreateOneFeedback: React.FC<CreateOneFeedbackProps> = ({ taskId, onComplete, viewType }) => {
  const navigate = useNavigate();
  const intl = useIntl();

  const { getOneTemplate, createOneFeedback } = useFeedback();
  const [command, setCommand] = React.useState<FeedbackApi.CreateFeedbackCommand>();
  const [template, setTemplate] = React.useState<FeedbackApi.FeedbackTemplate>();
  const [reply, setReply] = React.useState<string>('');

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

        {
          viewType === 'FEEDBACK_EDITOR_VIEW' &&
          <>
            <Box display='flex' alignItems='center'>
              <Typography variant='h3' fontWeight='bold' mr={3}>{intl.formatMessage({ id: 'feedback.title' })}</Typography>
              <StatusIndicator size='LARGE' taskId={taskId} />
            </Box>
            <Divider sx={{ my: 2 }} />
            <Typography variant='body2'>{intl.formatMessage({ id: 'feedback.sourceTaskId' })}{': '}{taskId}</Typography>
            <Typography variant='body2'>{intl.formatMessage({ id: 'feedback.formName' })}{': '}{template?.questionnaire.metadata.label}</Typography>
            <Typography variant='body2'>{intl.formatMessage({ id: 'feedback.dateReceived' })}{': '}{template?.questionnaire.metadata.completed}</Typography>
            <Typography variant='body2'>{intl.formatMessage({ id: 'feedback.createdBy' })}{': '}Amanda McDuff</Typography>
            <Typography variant='body2'>{intl.formatMessage({ id: 'feedback.updated' })}{': '}11.12.2024</Typography>
            <Typography variant='body2'>{intl.formatMessage({ id: 'feedback.updatedBy' })}{': '}John Smith</Typography>
            <Divider sx={{ my: 2 }} />
            <Typography variant='body2' fontWeight='bold'>{intl.formatMessage({ id: 'feedback.customerFeedback' })}</Typography>
          </>
        }

        <Typography variant='body2'>{intl.formatMessage({ id: 'feedback.category' })}{': '}{template?.labelValue},</Typography>
        <Typography variant='body2'>{intl.formatMessage({ id: 'feedback.subCategory' })}{': '}{template?.subLabelValue}</Typography>
        <Typography mt={2}><ReactMarkdown>{command.content}</ReactMarkdown></Typography>

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