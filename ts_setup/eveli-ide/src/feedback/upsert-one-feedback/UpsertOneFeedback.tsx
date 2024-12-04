import React from 'react';
import { Divider, TextField, Typography } from '@mui/material';
import * as Burger from '@/burger';
import { useFeedback, FeedbackApi } from '../feedback-api';

export interface UpsertOneFeedbackProps {
  taskId: string;
  onComplete: (createdFeedback: FeedbackApi.Feedback) => void;
}

export const UpsertOneFeedback: React.FC<UpsertOneFeedbackProps> = ({ taskId, onComplete }) => {
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

  if (!command) {
    return <>Loading command....</>
  }

  return (
    <>
      <div style={{ display: 'flex', flexDirection: 'column', padding: 10 }}>
        <Typography variant='h2'>Public reply to customer feedback</Typography>
        <Typography variant='body2'>Source task id: {taskId}</Typography>
        <Typography variant='body2'>Form name: {template?.questionnaire.metadata.label}</Typography>
        <Typography variant='body2'>Date feedback received from customer: {template?.questionnaire.metadata.completed}</Typography>

        <Divider sx={{ mt: 2 }} />

        <Typography variant='h2'>Customer feedback</Typography>
        <Typography variant='body2'>Category: {command.labelValue},</Typography>
        <Typography variant='body2'>Sub-category: {command.subLabelValue}</Typography>
        <Typography mt={2}>{command.content}</Typography>

        <Typography mt={2}>My reply to customer</Typography>
        <TextField onChange={(e) => setReply(e.target.value)}
          sx={{ mb: 3 }}
          multiline
          minRows={4}
          placeholder='Write a reply here'
          value={reply}
        />
      </div>
      <Burger.SecondaryButton onClick={() => console.log('cancel')} label='button.cancel' />
      <Burger.PrimaryButton onClick={handlePublish} label='button.publish' />
    </>
  )
}