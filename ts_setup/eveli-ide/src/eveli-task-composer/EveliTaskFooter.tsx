import React from 'react';

import { Box, Button, Grid2, Paper, Stack, Typography, useTheme, lighten } from "@mui/material";
import CloseIcon from '@mui/icons-material/Close';
import CheckIcon from '@mui/icons-material/Check';
import { useNavigate } from '@tanstack/react-router';

import { FormattedMessage } from 'react-intl';

import { DialobReview } from '@/dialob-review';
import { EveliPermissions } from '@/eveli-permissions';
import { EveliDateTimeFormatter } from "@/eveli-datetime-formatter";
import { TaskApi } from '@/api-task';
import { TaskFormDelegateProps } from './TaskFormState';
import { EveliTenantFeatureEnabled } from '@/api-tenant-config';
import { EveliTaskFeature } from '@/eveli-task-feature';




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

const FeedbackButton: React.FC<{ task: TaskApi.Task }> = ({ task }) => {
  const navigate = useNavigate();

  function handleFeedback() {
    navigate({
      from: '/secured/$locale/worker',
      params: { feedbackId: `${task.id}` },
      to: '/secured/$locale/worker/feedback/$feedbackId'
    });
  }

  return (
    <EveliTaskFeature id='TASK_FEEDBACK'>
      <EveliPermissions id='NAV_TO_TASKS_FEEDBACK'>
        <Button onClick={handleFeedback} variant='contained'><FormattedMessage id='task.form.feedback.manage' /></Button>
      </EveliPermissions>
    </EveliTaskFeature>);
}

export const FormReviewButton: React.FC<{task: { id: string, questionnaireId?: string | undefined }}> = ({ task }) => {
  const [open, setOpen] = React.useState(false);

  if(!task.questionnaireId) {
    return (<></>);
  }

  return (
    <>
      <EveliTenantFeatureEnabled id='FORM_REVIEW_FLASHY'>
        <Button sx={{ padding: '15px', marginTop: '15px', width: '100%',  
            animation: 'pulse 1.5s ease-in-out infinite',
            transition: 'transform 0.3s ease-in-out',
            '@keyframes pulse': {
              '0%': { transform: 'scale(1)', opacity: 1 },
              '50%': { transform: 'scale(1.05)', opacity: 0.8 },
              '100%': { transform: 'scale(1)', opacity: 1 },
          }}} 
          onClick={() => setOpen(true)} variant='contained'><FormattedMessage id='task.form.review' /></Button>
      </EveliTenantFeatureEnabled>

      <EveliTenantFeatureEnabled id='FORM_REVIEW_NORMAL'>
        <Button sx={{ padding: '15px', marginTop: '15px', width: '100%'}} onClick={() => setOpen(true)} variant='contained'>
          <FormattedMessage id='task.form.review' />
        </Button>
      </EveliTenantFeatureEnabled>

      {open && <DialobReview taskId={task.id} questionnaireId={task.questionnaireId} onClose={() => setOpen(false)} />}
    </>
  )
}


export interface EveliTaskFooterProps {
  form: TaskFormDelegateProps;
  task: TaskApi.Task | undefined | null;
  readOnly: boolean
}

export const EveliTaskFooter: React.FC<EveliTaskFooterProps> = (props) => {
  const { task, readOnly, form } = props;
  const updatedAt = task?.updated;
  const updatedBy = task?.updaterId;
  const theme = useTheme();


  return (
    <>
    {updatedAt && <Paper elevation={2} sx={{ p: 2, mb: 2 }}>
      <Grid2 container spacing={2}>
        <Grid2 size={{ xs: 12, md: 6 }} container justifyContent="flex-start">
          <Typography variant="caption" display="flex" gutterBottom>
            <FormattedMessage id='task.updated' />:&nbsp;<EveliDateTimeFormatter value={updatedAt} variant='text'/>&nbsp;&nbsp;
            {updatedBy || ''}
          </Typography>
        </Grid2>
      </Grid2>
    </Paper> }

    <Box sx={{
      bottom: 15,
      right: 16,
      zIndex: 1100,
      position: 'fixed'
    }}>
      <Paper  sx={{ border: 'unset', padding: theme.spacing(2), marginRight: theme.spacing(1), backgroundColor: lighten(theme.palette.primary.main, 0.9)}}>
        <Stack direction="row" spacing={1} justifyContent='flex-end'>

          <NavigateToTasksButton />

          {task?.questionnaireId && (
            <Box display='flex' gap={1}>

              <FeedbackButton task={task} />
            </Box>
          )}

          {!readOnly && <Button variant='contained' endIcon={<CheckIcon />} disabled={form.isSubmitting || !form.isValid || !form.dirty} onClick={form.onSubmit} >
            <FormattedMessage id='taskButton.accept' />
          </Button>}
        </Stack>
      </Paper>
    </Box>
    </>
  );
}
