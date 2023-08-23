import React from 'react';
import { Box, Stack, Grid } from '@mui/material';

import TaskClient from '@taskclient';
import { StyledFullScreenDialog } from '../StyledFullScreenDialog';
import Fields from './WorkOnTaskFields';
import Burger from '@the-wrench-io/react-burger';

const Left: React.FC<{}> = () => {

  return (
    <Box>
      <Fields.Form />
    </Box>)
}

const Right: React.FC<{}> = () => {

  return (
    <Stack spacing={2} direction='column'>
      <Fields.Checklist />
      <Fields.Attachments />
      <Fields.Messages />
    </Stack>
  );

}


const Header: React.FC<{}> = () => {

  return (<>

    <Grid container>
      <Grid item md={6} lg={6}>
        <Stack spacing={1} direction='column'>
          <Fields.Title />
          <Fields.Description />
        </Stack>
      </Grid>

      <Grid item md={6} lg={6} alignSelf='center'>
        <Stack spacing={1} direction='column'>
          <Fields.StartDate />
          <Fields.DueDate />
        </Stack>
      </Grid>
    </Grid >
  </>
  )
}



const Footer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { } = TaskClient.useTaskEdit();
  return (
    <>
      <Burger.PrimaryButton label='core.taskOps.workOnTask.button.reject' onClick={onClose} sx={{ backgroundColor: 'error.main', ':hover': { backgroundColor: 'error.dark' } }} />
      <Burger.PrimaryButton label='core.taskOps.workOnTask.button.complete' onClick={onClose} sx={{ backgroundColor: 'success.main', ':hover': { backgroundColor: 'success.dark' } }} />
      <Burger.PrimaryButton label='core.taskOps.workOnTask.button.edit' onClick={onClose} sx={{ backgroundColor: 'warning.main', ':hover': { backgroundColor: 'warning.dark' } }} />
      <Burger.PrimaryButton label='core.taskOps.workOnTask.button.cancel' onClick={onClose} />
    </>
  )
}


const WorkOnTaskDialog: React.FC<{ open: boolean, onClose: () => void, task?: TaskClient.TaskDescriptor }> = (props) => {

  if (!props.open || !props.task) {
    return null;
  }

  return (
    <TaskClient.EditProvider task={props.task}>
      <StyledFullScreenDialog
        header={<Header />}
        footer={<Footer onClose={props.onClose} />}
        left={<Left />}
        right={<Right />}
        onClose={props.onClose}
        open={props.open}
      />
    </TaskClient.EditProvider>);
}

export { WorkOnTaskDialog }