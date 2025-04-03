import React from 'react';
import { Box, Button, lighten, Paper, Stack, useTheme } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import CheckIcon from '@mui/icons-material/Check';
import { useNavigate } from '@tanstack/react-router';
import { FormattedMessage } from 'react-intl';

import { DialobReview } from '../dialob-review';
import { EveliPermissions } from '@/eveli-permissions';
import { TaskApi } from '@/api-task';





const NavigateToTasksButton: React.FC = () => {
  const navigate = useNavigate();
  function handleBack() {
    navigate({
      from: '/secured/$locale/worker',
      to: '/secured/$locale/worker/tasks'
    });
  }
  return (<Button variant='outlined' endIcon={<CloseIcon />}  onClick={handleBack}><FormattedMessage id='taskButton.cancel' /></Button>)
}

const FeedbackButton: React.FC<{ taskId: string | undefined }> = ({ taskId }) => {
  const navigate = useNavigate();

  function handleFeedback() {
    navigate({
      from: '/secured/$locale/worker',
      params: { feedbackId: `${taskId}` },
      to: '/secured/$locale/worker/feedback/$feedbackId'
    });
  }

  return (<Button onClick={handleFeedback} variant='contained'><FormattedMessage id='task.form.feedback.manage' /></Button>);
}

const FormReviewButton: React.FC<{ sessionId: string | undefined, taskId: string | undefined }> = ({ sessionId, taskId }) => {
  const [open, setOpen] = React.useState(false);

  if (!sessionId || !taskId) {
    return (<></>)
  }

  return (
    <>
      <Button onClick={() => setOpen(true)} variant='contained'><FormattedMessage id='task.form.review' /></Button>
      {open && <DialobReview taskId={taskId + ""} onClose={() => setOpen(false)} />}
    </>
  )
}

export interface EveliStickTaskButtonsProps {
  editTask: TaskApi.Task;
  isSubmitting: boolean;
  isValid: boolean;
  dirty: boolean;
  readonly: boolean;
  submitForm: () => Promise<void>;
}

export const EveliStickyTaskButtons: React.FC<EveliStickTaskButtonsProps> = ({ editTask, isSubmitting, isValid, dirty, submitForm, readonly }) => {
  const theme = useTheme();


  return (
    <Box sx={{
      bottom: 10,
      width: 'fit-content',
      height: 'fit-content',
      top: 35,
      right: 16,
      zIndex: 1100,
      position: 'fixed'
    }}>
      <Paper  sx={{ border: 'unset', padding: theme.spacing(2), marginRight: theme.spacing(1), backgroundColor: lighten(theme.palette.primary.main, 0.9)}}>
        <Stack direction="row" spacing={1} justifyContent='flex-end'>

          <NavigateToTasksButton />

          {(!editTask.keyWords || editTask.keyWords.length === 0) && (
            <Box display='flex' gap={1}>
              <FormReviewButton sessionId={editTask.questionnaireId} taskId={editTask.id} />
              <EveliPermissions id='NAV_TO_TASKS_FEEDBACK'><FeedbackButton taskId={editTask.id} /></EveliPermissions>
            </Box>
          )}
          {!readonly && <Button variant='contained' endIcon={<CheckIcon />} disabled={isSubmitting || !isValid || !dirty} onClick={submitForm} >
            <FormattedMessage id='taskButton.accept' />
          </Button>}
        </Stack>
      </Paper>
    </Box>

  )
}