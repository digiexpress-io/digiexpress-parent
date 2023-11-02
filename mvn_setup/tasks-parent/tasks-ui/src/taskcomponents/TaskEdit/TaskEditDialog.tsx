import React from 'react';
import { Box, List, Button, Typography, Stack } from '@mui/material';
import SecurityIcon from '@mui/icons-material/Security';
import { FormattedMessage } from 'react-intl';
import StyledFullScreenDialog from '../Dialogs';
import Fields from './TaskEditFields';
import Events from './TaskEvents';
import Burger from '@the-wrench-io/react-burger';
import Context from 'context';
import { TaskDescriptor } from 'taskdescriptor';
import Section from 'section';

const Left: React.FC<{}> = () => {
  const { state } = Context.useTaskEdit();

  return (
    <>
      <Section>
        <Typography fontWeight='bold'><FormattedMessage id='core.taskEdit.fields.title' /></Typography>
        <Fields.Title />
      </Section>

      <Section>
        <Typography fontWeight='bold'><FormattedMessage id='core.taskEdit.fields.description' /></Typography>
        <Fields.Description />
      </Section>

      <Stack spacing={1} direction='row'>
        <Section>
          <Typography fontWeight='bold'><FormattedMessage id='core.taskEdit.fields.startDate' /></Typography>
          <Fields.StartDate />
        </Section>
        <Section>
          <Typography fontWeight='bold'><FormattedMessage id='core.taskEdit.fields.dueDate' /></Typography>
          <Fields.DueDate />
        </Section>
      </Stack>


      <Stack spacing={1} direction='row'>
        <Section>
          <Typography fontWeight='bold'><FormattedMessage id='core.taskEdit.fields.status' /></Typography>
          <Fields.Status />
        </Section>
        <Section>
          <Typography fontWeight='bold'><FormattedMessage id='core.taskEdit.fields.priority' /></Typography>
          <Fields.Priority />
        </Section>
      </Stack>

      <Stack spacing={1} direction='row'>
        <Section>
          <Typography fontWeight='bold'><FormattedMessage id='core.taskEdit.fields.assignees' /></Typography>
          <Fields.Assignee />
        </Section>
        <Section>
          <Typography fontWeight='bold'><FormattedMessage id='core.taskEdit.fields.roles' /></Typography>
          <Fields.Roles />
        </Section>
      </Stack>
    </>)
}

const Right: React.FC<{}> = () => {
  const { state } = Context.useTaskEdit();
  console.log(state.events);
  return (
    <>

      <Fields.Checklist />

      <Section>
        <Typography fontWeight='bold'><FormattedMessage id='core.taskEdit.fields.history' /></Typography>
        <List>
          {state.events
            .map((event, index) => <Events key={index} event={event} />)}
        </List>
      </Section>
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
      <Button startIcon={<SecurityIcon />} variant='contained' color='warning'><FormattedMessage id='core.taskEdit.clientData' /></Button>
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
    <Context.EditProvider task={props.task}>
      <StyledFullScreenDialog
        header={<Header onClose={handleClose} />}
        footer={<Footer onClose={handleClose} />}
        left={<Left />}
        right={<Right />}
        onClose={handleClose}
        open={props.open}
      />
    </Context.EditProvider>);
}

export { TaskEditDialog }