import React from 'react';

import { Box, Button, Grid2, Paper, Stack, Typography, useTheme, lighten } from "@mui/material";
import CloseIcon from '@mui/icons-material/Close';
import CheckIcon from '@mui/icons-material/Check';
import { useNavigate } from '@tanstack/react-router';

import { FormattedMessage } from 'react-intl';
import { DateTimeFormatter } from "@dxs-ts/xui-datetime";
import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';
import { TaskFormDelegateProps } from './TaskFormState';


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

export const FormReviewButton: React.FC<{task: { id: string, questionnaireId?: string | undefined }}> = ({ task }) => {
  const backend = useTaskBackend();
  if(!task.questionnaireId) {
    return (<></>);
  }
  return (<backend.slots.DialobReview task={task}/>)
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


  return (
    <>
    {updatedAt && <Paper elevation={2} sx={{ p: 2, mb: 2 }}>
      <Grid2 container spacing={2}>
        <Grid2 size={{ xs: 12, md: 6 }} container justifyContent="flex-start">
          <Typography variant="caption" display="flex" gutterBottom>
            <FormattedMessage id='task.updated' />:&nbsp;<DateTimeFormatter value={updatedAt} variant='text'/>&nbsp;&nbsp;
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

          {!readOnly && <Button variant='contained' endIcon={<CheckIcon />} disabled={form.isSubmitting || !form.isValid || !form.dirty} onClick={form.onSubmit} >
            <FormattedMessage id='taskButton.accept' />
          </Button>}
        </Stack>
      </Paper>
    </Box>
    </>
  );
}
