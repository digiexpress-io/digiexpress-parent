import React from 'react';
import { Box, List, Button, Typography } from '@mui/material';
import SecurityIcon from '@mui/icons-material/Security';
import { FormattedMessage } from 'react-intl';
import StyledFullScreenDialog from '../Dialogs';
import Fields from './TaskEditFields';
import Events from './TaskEvents';
import Burger from '@the-wrench-io/react-burger';
import Context from 'context';
import { TaskDescriptor } from 'taskdescriptor';
import Section from '../Section';

const Left: React.FC<{}> = () => {
  const { state } = Context.useTaskEdit();

  return (
    <>
      <Box />
      <Section>
        <Typography fontWeight='bold'><FormattedMessage id='core.taskEdit.fields.title' /></Typography>
        <Fields.Title />
      </Section>

      <Section>
        <Typography fontWeight='bold'><FormattedMessage id='core.taskEdit.fields.description' /></Typography>
        <Fields.Description />
      </Section>

      <Section>
        <Typography fontWeight='bold'><FormattedMessage id='core.taskEdit.fields.startDate' /></Typography>
        <Fields.StartDate onClick={() => { }} />
      </Section>

      <Section>
        <Typography fontWeight='bold'><FormattedMessage id='core.taskEdit.fields.dueDate' /></Typography>
        <Fields.DueDate onClick={() => { }} />
      </Section>

      <Section>
        <Typography fontWeight='bold'><FormattedMessage id='core.taskEdit.fields.status' /></Typography>
        <Fields.Status />
      </Section>

      <Section>
        <Typography fontWeight='bold'><FormattedMessage id='core.taskEdit.fields.priority' /></Typography>
        <Fields.Priority />
      </Section>

      <Section>
        <Typography fontWeight='bold'><FormattedMessage id='core.taskEdit.fields.assignees' /></Typography>
        <Fields.Assignee />
      </Section>

      <Section>
        <Typography fontWeight='bold'><FormattedMessage id='core.taskEdit.fields.roles' /></Typography>
        <Fields.Roles />
      </Section>

      {state.task.checklist.length > 0 && <Section>
        <Typography fontWeight='bold'><FormattedMessage id='core.taskEdit.fields.checklist' /></Typography>
        <Fields.Checklist />
      </Section>}
    </>)
}

const Right: React.FC<{}> = () => {
  const { state } = Context.useTaskEdit();
  return (<List>
    {state.events
      .map((event, index) => <Events key={index} event={event} />)}
  </List>);
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