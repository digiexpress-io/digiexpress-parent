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
  const { activeTab } = TaskClient.useMenu();

  return (
    <Box>
      {activeTab === 'messages' && <Fields.Messages />}
      {activeTab === 'attachments' && <Fields.Attachments />}
      {activeTab === 'checklists' && <Fields.Checklist />}
    </Box>
  );

}


const Header: React.FC<{ onClose: () => void }> = ({ onClose }) => {

  return (<>

    <Grid container>
      <Grid item md={6} lg={6}>
        <Stack spacing={1} direction='column'>
          <Fields.Title />
          <Fields.Description />
        </Stack>
      </Grid>

      <Grid item md={2} lg={2} alignSelf='center'>
        <Stack spacing={1} direction='column'>
          <Fields.StartDate />
          <Fields.DueDate />
        </Stack>
      </Grid>

      <Grid item md={3} lg={3} alignSelf='center'>
        <Fields.Menu />
      </Grid>

      <Grid item md={1} lg={1} sx={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center' }}>
        <Fields.CloseDialogButton onClose={onClose} />
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
        header={<Header onClose={props.onClose} />}
        footer={<Footer onClose={props.onClose} />}
        left={<Left />}
        right={<Right />}
        onClose={props.onClose}
        open={props.open}
      />
    </TaskClient.EditProvider>);
}

export { WorkOnTaskDialog }