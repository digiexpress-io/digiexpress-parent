import React from 'react';
import { Box, CircularProgress, Divider, TextField, Typography, useTheme, Button, Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material';

import { useIntl, FormattedMessage } from 'react-intl';
import { DateTimeFormatter } from '@dxs-ts/xui-datetime';

import { useFeedback, FeedbackApi } from '../api-feedback';
import { StatusIndicator } from '../status-indicator';
import { ApprovalCount } from '../approval-count';
import { FeedbackContent } from './FeedbackContent';


export interface UpdateOneFeedbackProps {
  taskRef: string;
  taskId: string;
  onComplete: (createdFeedback: FeedbackApi.Feedback) => void;
  onDelete: () => void;
  
  allowDelete?: boolean;
  slots?: {
    AcceptButton: React.ElementType<{ disabled: boolean, onClick: () => Promise<void> }>;
    CancelButton: React.ElementType<{  disabled: boolean, onClick: () => Promise<void> }>;
  }
}


export const UpdateOneFeedback: React.FC<UpdateOneFeedbackProps> = ({ slots, taskRef, taskId, onComplete, onDelete, allowDelete = true }) => {

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


  const mapToLocalState = React.useCallback((resp: FeedbackApi.Feedback | undefined) => {
    setFeedback(resp);
    setReply(resp?.replyText ?? '');
    setQuestion(resp?.content?.question ?? '');
    setSavedReply(resp?.replyText ?? '');

    setMain({ labelKey: resp?.labelKey ?? '', labelValue: resp?.labelValue ?? '' })
    setSub({ subLabelKey: resp?.subLabelKey ?? '', subLabelValue: resp?.subLabelValue ?? '' })

    setCustomerTitle(resp?.customerTitle ?? '')
  }, []);



  React.useEffect(() => {
    getOneFeedback(taskRef).then(mapToLocalState);
  }, []);

  const handlePublish = React.useCallback(async function() {
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

    return modifyOneFeedback(taskRef, command).then(updatedFeedback => {
      onComplete(updatedFeedback);
      setSavedReply(reply);
      mapToLocalState(updatedFeedback)
    });
  }, [main, sub, customerTitle, taskRef, reply, question, feedback?.id])


  function confirmDelete() {
    deleteOneFeedback(taskRef).then(feedback => {
      onComplete(feedback);
    });
    onDelete();
  }

  if (!feedback) {
    return (<CircularProgress />)
  }

  const isChanged = (
    reply !== savedReply ||
    question !== feedback.content?.question ||
    customerTitle !== feedback?.customerTitle ||
    main.labelKey !== feedback?.labelKey ||
    main.labelValue !== feedback?.labelValue ||
    sub.subLabelKey !== feedback?.subLabelKey ||
    sub.subLabelValue !== feedback?.subLabelValue
  );

  const AcceptButton: React.ElementType<{ disabled: boolean, onClick: () => Promise<void> }> = slots?.AcceptButton ?? SaveFeedback;
  const CancelButton: React.ElementType<{ disabled: boolean, onClick: () => Promise<void> }> =  slots?.CancelButton ?? CancelFeedback;

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
        <DateTimeFormatter value={feedback.updatedOnDate} variant='text' />
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

        <CancelButton onClick={async () => setReply(savedReply)} disabled={!isChanged}>
          <FormattedMessage id='button.cancel' />
        </CancelButton>

        <AcceptButton onClick={handlePublish} disabled={!isChanged}>
          <FormattedMessage id='button.update' />
        </AcceptButton>
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
            <Button variant="outlined" onClick={() => setConfirmOpen(false)}>
              <FormattedMessage id='button.cancel' />
            </Button>
            <Button onClick={confirmDelete} color='error'>
              <FormattedMessage id='button.confirmDelete' />
            </Button>
          </DialogActions>
        </Dialog>
      )}

    </div>
  )
}

const SaveFeedback: React.FC<{ disabled: boolean, onClick: () => Promise<void> }> = ({ disabled, onClick }) => {
  return (
    <Button variant='contained' onClick={onClick} disabled={disabled}>
      <FormattedMessage id='button.update' />
    </Button>
  )
}

const CancelFeedback: React.FC<{ disabled: boolean, onClick: () => Promise<void> }> = ({ disabled, onClick }) => {
  return (
    <Button variant="outlined" onClick={onClick} disabled={disabled}>
      <FormattedMessage id='button.cancel' />
    </Button>
  )
}
