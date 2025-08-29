import React from 'react';

import { Box, Button, Grid2, Paper, Stack, Typography, useTheme, lighten, Dialog, DialogTitle, DialogContent, DialogActions } from "@mui/material";
import CloseIcon from '@mui/icons-material/Close';
import CheckIcon from '@mui/icons-material/Check';
import ArrowRightIcon from '@mui/icons-material/ArrowRight';

import { FormattedMessage } from 'react-intl';
import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';
import { TaskFormDelegateProps } from './TaskFormState';


const NavigateToTasksButton: React.FC = () => {
  const backend = useTaskBackend();
  function handleBack() {
    backend.navigate.findAllTasks()
  }
  return (<Button variant='outlined' endIcon={<CloseIcon />}  onClick={handleBack}><FormattedMessage id='taskButton.cancel' /></Button>)
}

export const FormReviewButton: React.FC<{task: { id: string, questionnaireId?: string | undefined }}> = ({ task }) => {
  
  const backend = useTaskBackend();
  const [open, setOpen] = React.useState(false);
  if(!task.questionnaireId) {
    return (<></>);
  }
 
  return (
    <>
      <backend.slots.DialobReviewButton onClick={() => setOpen(true)}   />
      <Dialog open={open} onClose={() => setOpen(false)} fullScreen>
        <DialogTitle><FormattedMessage id='dialobForm.review.title'/></DialogTitle>
        <DialogContent>
          <backend.slots.DialobReview task={task} onClose={() => setOpen(false)} />
        </DialogContent>
        <DialogActions>
          <Button variant='outlined' endIcon={<ArrowRightIcon/>} onClick={async () => {
            const url = await backend.persistence.getOneTaskPdfLink(task.questionnaireId!, task.id);
            window.open(url);
          }}>
            <FormattedMessage id='taskLink.pdf.open' />
          </Button>
          <Button variant='contained' onClick={() => setOpen(false)}><FormattedMessage id='button.close'/></Button>
        </DialogActions>
      </Dialog>
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
            <FormattedMessage id='task.updated' />:&nbsp;<backend.slots.DateTimeFormatter value={updatedAt} variant='text'/>&nbsp;&nbsp;
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
