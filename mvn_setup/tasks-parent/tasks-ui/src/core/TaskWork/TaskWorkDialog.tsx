import React from 'react';
import { Box, Stack, Grid } from '@mui/material';

import StyledFullScreenDialog from '../Dialogs';
import Fields from './TaskWorkFields';
import TaskClient from '@taskclient';
import Context from 'context';
import { MenuProvider, useMenu } from './menu-ctx';

const Left: React.FC<{}> = () => {

  return (
    <Box>
      <Fields.Form />
    </Box>)
}

const Right: React.FC<{}> = () => {
  const { activeTab } = useMenu();

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
      <Grid item md={6} lg={6} sx={{ mt: 0.5 }}>
        <Stack spacing={1} direction='column'>
          <Fields.Title />
          <Fields.Description />
        </Stack>
      </Grid>

      <Grid item md={3} lg={3}>
        <Fields.Menu />
      </Grid>


      <Grid item md={2} lg={2} sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', pb: 1 }}>
        <Stack spacing={1} direction='column'>
          <Fields.StartDate />
          <Fields.DueDate />
        </Stack>
      </Grid>

      <Grid item md={1} lg={1} sx={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', pb: 1 }}>
        <Fields.CloseDialogButton onClose={onClose} />
      </Grid>
    </Grid >
  </>
  )
}

const Footer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  return (
    <>
      <Fields.SplitButton onClose={onClose} />
    </>
  )
}

const TaskWorkDialog: React.FC<{ open: boolean, onClose: () => void, task?: Context.TaskDescriptor }> = (props) => {

  if (!props.open || !props.task) {
    return null;
  }

  return (
    <MenuProvider>
      <Context.EditProvider task={props.task}>
        <StyledFullScreenDialog
          header={<Header onClose={props.onClose} />}
          footer={<Footer onClose={props.onClose} />}
          left={<Left />}
          right={<Right />}
          onClose={props.onClose}
          open={props.open}
          shortHeader
        />
      </Context.EditProvider>
    </MenuProvider>
  );
}

export { TaskWorkDialog };
