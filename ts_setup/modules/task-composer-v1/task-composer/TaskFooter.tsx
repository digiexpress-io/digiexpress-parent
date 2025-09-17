import React from 'react';

import { Box, Button, Grid2, Paper, Stack, Typography, useTheme, lighten } from "@mui/material";
import CloseIcon from '@mui/icons-material/Close';
import CheckIcon from '@mui/icons-material/Check';

import { FormattedMessage, useIntl } from 'react-intl';
import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';
import { TaskFormDelegateProps } from './TaskFormState';
import { useLinkProps } from '@tanstack/react-router';


const NavigateToTasksButton: React.FC = () => {
  const backend = useTaskBackend();
  function handleBack() {
    backend.navigate.findAllTasks()
  }
  return (<Button variant='outlined' endIcon={<CloseIcon />} onClick={handleBack}><FormattedMessage id='taskButton.cancel' /></Button>)
}

export const FormReviewButton: React.FC<{ task: { id: string, questionnaireId?: string | undefined } }> = ({ task }) => {

  const backend = useTaskBackend();
  const currentLocale = useIntl();
  const { locale } = currentLocale;

  if (!task.questionnaireId) {
    return (<></>);
  }
 
  const linkProps = useLinkProps({
    to: '/secured/$locale/worker/tasks/$taskId/review',
    params: { taskId: task.id, locale },
    search: { mode: 'CONTENT_ONLY' }
  });

  function handleOpenReview() {
    if (linkProps.href) {
      window.open(linkProps.href, '_blank', 'noopener,noreferrer');
    }
  }

  return (<>
    <backend.slots.DialobReviewButton onClick={handleOpenReview} />
  </>);
}


export interface TaskFooterProps {
  form: TaskFormDelegateProps;
  task: TaskApi.Task | undefined | null;
  readOnly: boolean
}

export const TaskFooter: React.FC<TaskFooterProps> = (props) => {
  const { task, readOnly, form } = props;
  const updatedAt = task?.updated;
  const updatedBy = task?.updaterId;
  const theme = useTheme();
  const backend = useTaskBackend();

  return (
    <>
      {updatedAt && <Paper elevation={2} sx={{ p: 2, mb: 2 }}>
        <Grid2 container spacing={2}>
          <Grid2 size={{ xs: 12, md: 6 }} container justifyContent="flex-start">
            <Typography variant="caption" display="flex" gutterBottom>
              <FormattedMessage id='task.updated' />:&nbsp;<backend.slots.DateTimeFormatter value={updatedAt} variant='text' />&nbsp;&nbsp;
              {updatedBy || ''}
            </Typography>
          </Grid2>
        </Grid2>
      </Paper>}

      <Box sx={{
        bottom: 15,
        right: 16,
        zIndex: 1100,
        position: 'fixed'
      }}>
        <Paper sx={{ border: 'unset', padding: theme.spacing(2), marginRight: theme.spacing(1), backgroundColor: lighten(theme.palette.primary.main, 0.9) }}>
          <Stack direction="row" spacing={1} justifyContent='flex-end'>

            <NavigateToTasksButton />

            {!readOnly && <Button variant='contained' endIcon={<CheckIcon />} disabled={form.isSubmitting || !form.isValid || !form.dirty} onClick={form.onSubmit} >
              <FormattedMessage id='taskButton.accept' />
            </Button>}
          </Stack>
        </Paper>
      </Box>
    </>
  );
}
