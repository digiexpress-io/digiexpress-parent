import React from 'react';
import { Box, CircularProgress, TextField, Typography, Button } from '@mui/material';
import { useIntl, FormattedMessage } from 'react-intl';
import { useFeedback, FeedbackApi } from '../api-feedback';
import { FeedbackContent } from './FeedbackContent';
import { UpsertOneFeedbackRoot, useUtilityClasses } from './useUtilityClasses';

export interface CreateOneFeedbackProps {
  taskRef: string;
  onComplete: (createdFeedback: FeedbackApi.Feedback) => void;
  onCancel: () => void;
  slots?: {
    AcceptButton: React.ElementType<{ disabled: boolean, onClick: () => Promise<void> }>;
    CancelButton: React.ElementType<{  disabled: boolean, onClick: () => Promise<void> }>;
  }
}

export const CreateOneFeedback: React.FC<CreateOneFeedbackProps> = ({ taskRef, onComplete, onCancel, slots }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

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
        reply: template.replys?.join("\r\n\r\n") ?? '',
        customerTitle: template.customerTitle,
        question: template.content?.question ?? '' 
      });
      setTemplate(template);
    });
  }, [getOneTemplate, taskRef]);

  function setReply(reply: string) {
    setCommand(prev => (prev ? { ...prev, reply } : undefined));
  }

  function setQuestion(question: string) {
    setCommand(prev => (prev ? { ...prev, question } : undefined));
  }

  const handlePublish = React.useCallback(async function() {
    if (command) {
      return createOneFeedback(taskRef, command).then(feedback => {
        onComplete(feedback);
      });
    }
  }, [command, taskRef, createOneFeedback, onComplete]);

  async function handleCancel() {
    if (template) {
      setReply(template.replys?.join("\r\n\r\n") ?? '')
    }
    onCancel();
  }

  if (!command || !template) {
    return <CircularProgress />
  }

  const AcceptButton: React.ElementType<{ disabled: boolean, onClick: () => Promise<void> }> =
    slots?.AcceptButton ?? SaveFeedback;
  const CancelButton: React.ElementType<{ disabled: boolean, onClick: () => Promise<void> }> =
    slots?.CancelButton ?? CancelFeedback;

  return (
    <>
      <UpsertOneFeedbackRoot className={classes.root}>
        <div className={classes.container}>
          <Typography variant='h3' className={classes.title}>
            {intl.formatMessage({ id: 'feedback.create.title' })}
          </Typography>

          <FeedbackContent
            feedback={{ ...template, ...command }}
            onChange={(next) => { setCommand(prev => (prev ? { ...prev, ...next } : undefined)) }}
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
              value={command?.question ?? ''}
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
              value={command?.reply ?? ''}
            />
          </div>
        </div>
      </UpsertOneFeedbackRoot>

      <Box className={classes.actions}>
        <CancelButton onClick={handleCancel} disabled={false} />
        <AcceptButton onClick={handlePublish} disabled={!command.reply}/>
      </Box>
    </>
  )
}

const SaveFeedback: React.FC<{ disabled: boolean, onClick: () => Promise<void>  }> = ({ disabled, onClick }) => {
  return (
    <Button variant='contained' disabled={disabled} onClick={onClick}><FormattedMessage id='button.publish' /></Button>
  )
}

const CancelFeedback: React.FC<{ disabled: boolean, onClick: () => Promise<void>  }> = ({ disabled, onClick }) => {
  return (
    <Button variant="outlined" disabled={disabled} onClick={onClick}><FormattedMessage id='button.cancel' /></Button>
  )
}
