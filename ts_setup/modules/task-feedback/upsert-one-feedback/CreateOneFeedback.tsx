import React from 'react';
import { Box, CircularProgress, TextField, Typography, Button } from '@mui/material';
import { useIntl, FormattedMessage } from 'react-intl';
import { useFeedback, FeedbackApi } from '../api-feedback';
import { FeedbackContent } from './FeedbackContent';

export interface CreateOneFeedbackProps {
  taskRef: string;
  onComplete: (createdFeedback: FeedbackApi.Feedback) => void;
  onCancel: () => void;
}

export const CreateOneFeedback: React.FC<CreateOneFeedbackProps> = ({ taskRef, onComplete, onCancel }) => {

  const intl = useIntl();

  const { getOneTemplate, createOneFeedback } = useFeedback();
  const [command, setCommand] = React.useState<FeedbackApi.CreateFeedbackCommand>();
  const [template, setTemplate] = React.useState<FeedbackApi.FeedbackTemplate>();

  React.useEffect(() => {
    getOneTemplate(taskRef!).then(template => {

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
        reply: template.replys?.join("\r\n\r\n") ?? '',
        customerTitle: template.customerTitle,
        question: template.content?.question ?? '' 
      });

      setTemplate(template);

    });

  }, []);



  function setReply(reply: string) {
    setCommand(prev => (prev ? { ...prev, reply } : undefined));
  }

  function setQuestion(question: string) {
    setCommand(prev => (prev ? { ...prev, question } : undefined));
  }


  function handlePublish() {
    if (command) {
      createOneFeedback(taskRef, command).then(feedback => {
        onComplete(feedback);
      });
    }
  }

  
  function handleCancel() {
    if (template) {
      setReply(template.replys?.join("\r\n\r\n") ?? '')
    }
    onCancel();
  }

  if (!command || !template) {
    return <CircularProgress />
  }


  return (
    <>
      <div style={{ display: 'flex', flexDirection: 'column', padding: 10 }}>
        <Typography variant='h3' fontWeight='bold' mr={3}>{intl.formatMessage({ id: 'feedback.create.title' })}</Typography>
        <FeedbackContent feedback={{ ...template, ...command }} onChange={(next) => { setCommand(prev => (prev ? { ...prev, ...next } : undefined)) }} />

        <Typography fontWeight='bold'>{intl.formatMessage({ id: 'feedback.feedbackValue' })}</Typography>
        <TextField onChange={(e) => setQuestion(e.target.value)}
          sx={{ mb: 3 }}
          multiline
          minRows={4}
          placeholder={intl.formatMessage({ id: 'feedback.feedbackValue.placeholder', defaultMessage: 'Customer question' })}
          value={command?.question ?? ''}
        />

        <Typography mt={2} fontWeight='bold'>{intl.formatMessage({ id: 'feedback.myReply' })}</Typography>
        <TextField onChange={(e) => setReply(e.target.value)}
          sx={{ mb: 3 }}
          multiline
          minRows={4}
          placeholder='Write a reply here'
          value={command?.reply ?? ''}
        />

      </div>
      <Box display='flex' gap={1}>
        <Button variant="outlined" onClick={handleCancel}><FormattedMessage id='button.cancel' /></Button>
        <Button variant='contained' onClick={handlePublish}><FormattedMessage id='button.publish' /></Button>
      </Box>
    </>
  )
}