import React from 'react';
import { Box, CircularProgress, TextField, Typography, Button } from '@mui/material';
import { useIntl, FormattedMessage } from 'react-intl';
import ReactMarkdown from 'react-markdown';
import { useNavigate } from 'react-router-dom';

import * as Burger from '@/burger';
import { useFeedback, FeedbackApi } from '../feedback-api';


export interface CreateOneFeedbackProps {
  taskId: string;
  onComplete: (createdFeedback: FeedbackApi.Feedback) => void;
}

export const CreateOneFeedback: React.FC<CreateOneFeedbackProps> = ({ taskId, onComplete }) => {
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
        taskId: template.taskId,
        userId: template.userId,
        subLabelKey: template.subLabelKey,
        subLabelValue: template.subLabelValue,
        reply: template.replys?.toString(),
        customerTitle: template.customerTitle
      });

      setTemplate(template);
      setReply(template.replys?.join("\r\n\r\n"));
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
        <Typography variant='h3' fontWeight='bold' mr={3}>{intl.formatMessage({ id: 'feedback.create.title' })}</Typography>

        <Typography variant='body2'>{intl.formatMessage({ id: 'feedback.category' })}{': '}{template?.labelValue}</Typography>
        <Typography variant='body2'>{intl.formatMessage({ id: 'feedback.subCategory' })}{': '}{template?.subLabelValue}</Typography>
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
        ) : (
          <Box p={2}>
            <Typography variant='body2' fontStyle='italic'>{intl.formatMessage({ id: 'feedback.noFeedback.info1' })}</Typography>
            <Typography variant='body2' fontStyle='italic'>{intl.formatMessage({ id: 'feedback.noFeedback.info2' })}</Typography>
          </Box>
        )
        }
      </div>
      <Box display='flex' gap={1}>
        <Button onClick={handleCancel}  variant='text'><FormattedMessage id='button.cancel'/></Button>
        <Button variant='contained' onClick={handlePublish} disabled={!reply}><FormattedMessage id='button.publish'/></Button>
      </Box>
    </>
  )
}