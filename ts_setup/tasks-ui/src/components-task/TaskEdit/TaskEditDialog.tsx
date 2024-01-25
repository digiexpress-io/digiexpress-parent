import React from 'react';
import { Box, List, Button, Typography, Stack } from '@mui/material';
import CrmIcon from '@mui/icons-material/AdminPanelSettingsOutlined';
import { FormattedMessage } from 'react-intl';
import StyledFullScreenDialog from '../Dialogs';
import Fields from './TaskEditFields';
import Events from './TaskEvents';
import Burger from 'components-burger';
import Context from 'context';
import { TaskDescriptor, TaskEditProvider } from 'descriptor-task';

const Left: React.FC<{}> = () => {
  return (
    <>
      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='core.taskEdit.fields.title' /></Typography>
        <Fields.Title />
      </Burger.Section>

      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='core.taskEdit.fields.description' /></Typography>
        <Fields.Description />
      </Burger.Section>

      <Stack spacing={1} direction='row'>
        <Burger.Section>
          <Typography fontWeight='bold'><FormattedMessage id='core.taskEdit.fields.startDate' /></Typography>
          <Fields.StartDate />
        </Burger.Section>
        <Burger.Section>
          <Typography fontWeight='bold'><FormattedMessage id='core.taskEdit.fields.dueDate' /></Typography>
          <Fields.DueDate />
        </Burger.Section>
      </Stack>


      <Stack spacing={1} direction='row'>
        <Burger.Section>
          <Typography fontWeight='bold'><FormattedMessage id='core.taskEdit.fields.status' /></Typography>
          <Fields.Status />
        </Burger.Section>
        <Burger.Section>
          <Typography fontWeight='bold'><FormattedMessage id='core.taskEdit.fields.priority' /></Typography>
          <Fields.Priority />
        </Burger.Section>
      </Stack>

      <Stack spacing={1} direction='row'>
        <Burger.Section>
          <Typography fontWeight='bold'><FormattedMessage id='core.taskEdit.fields.assignees' /></Typography>
          <Fields.Assignee />
        </Burger.Section>
        <Burger.Section>
          <Typography fontWeight='bold'><FormattedMessage id='core.taskEdit.fields.roles' /></Typography>
          <Fields.Roles />
        </Burger.Section>
      </Stack>
    </>)
}

const Right: React.FC<{}> = () => {
  const { events } = Context.useTaskEdit();

  return (
    <>
      <Fields.Checklist />

      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='core.taskEdit.fields.history' /></Typography>
        <List>
          {events.map((event, index) => <Events key={index} event={event} />)}
        </List>
      </Burger.Section>
    </>
  );
}


const Header: React.FC<{ onClose: () => void }> = ({ onClose }) => {

  return (
    <Box display='flex' alignItems='center'>
      <Typography variant='h4'><FormattedMessage id='taskEditDialog.title' /></Typography>
      <Box flexGrow={1} />
      <Fields.CloseDialogButton onClose={onClose} />
    </Box>
  )
}

const Footer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  return (
    <>
      <Button startIcon={<CrmIcon />} variant='contained' color='warning'><FormattedMessage id='core.taskEdit.clientData' /></Button>
      <Burger.PrimaryButton label='buttons.accept' onClick={onClose} />
    </>
  )
}

const TaskEditDialog: React.FC<{ open: boolean, onClose: () => void, task?: TaskDescriptor }> = (props) => {
  const tasks = Context.useTasks();

  if (!props.open || !props.task) {
    return null;
  }

  function handleClose() {
    tasks.reload().then(() => props.onClose());
  }

  return (
    <TaskEditProvider task={props.task.entry}>
      <StyledFullScreenDialog
        header={<Header onClose={handleClose} />}
        footer={<Footer onClose={handleClose} />}
        left={<Left />}
        right={<Right />}
        onClose={handleClose}
        open={props.open}
      />
    </TaskEditProvider>);
}

export { TaskEditDialog }