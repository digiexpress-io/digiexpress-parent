import React from 'react';
import { Box, Stack, List, Dialog, Button, Grid } from '@mui/material';
import SecurityIcon from '@mui/icons-material/Security';
import { FormattedMessage } from 'react-intl';
import TaskClient from '@taskclient';
import DatePicker from '../DatePicker';
import StyledFullScreenDialog from '../Dialogs';
import Fields from './TaskEditFields';
import Events from './TaskEvents';
import Burger from '@the-wrench-io/react-burger';

const Left: React.FC<{}> = () => {

  return (
    <>
      <Box />
      <Fields.Title />
      <Fields.Description />
      <Fields.Checklist />
    </>)
}

const Right: React.FC<{}> = () => {
  const { state } = TaskClient.useTaskEdit();
  return (<List>
    {state.events.map(event => <Events event={event} />)}
  </List>);

}


const Header: React.FC<{ onClose: () => void }> = ({ onClose }) => {

  const { state } = TaskClient.useTaskEdit();
  const [datePickerOpen, setDatePickerOpen] = React.useState(false);
  const [startDate, setStartDate] = React.useState<Date | string | undefined>(state.task.startDate);
  const [dueDate, setDueDate] = React.useState<Date | string | undefined>(state.task.dueDate);
  const [activeDate, setActiveDate] = React.useState<"start" | "due" | undefined>();

  const handleStartDateClick = () => {
    setActiveDate('start');
    setDatePickerOpen(true);
  }

  const handleDueDateClick = () => {
    setActiveDate('due');
    setDatePickerOpen(true);
  }

  return (<>
    <Dialog open={datePickerOpen} onClose={() => setDatePickerOpen(false)}>
      <DatePicker startDate={startDate} setStartDate={setStartDate} dueDate={dueDate} setDueDate={setDueDate} activeDate={activeDate} />
    </Dialog>

    <Grid container>
      <Grid item md={6} lg={6} alignSelf='center'>
        <Stack spacing={2} direction='row'>
          <Fields.Status />
          <Fields.Assignee />
          <Fields.Priority />
          <Fields.Options />
        </Stack>
      </Grid>

      <Grid item md={5} lg={5} alignSelf='center'>
        <Stack spacing={2} direction='row'>
          <Fields.StartDate onClick={handleStartDateClick} />
          <Fields.DueDate onClick={handleDueDateClick} />
        </Stack>
      </Grid>

      <Grid item md={1} lg={1} sx={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center' }}>
        <Fields.CloseDialogButton onClose={onClose} />
      </Grid>
    </Grid>
  </>
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


const TaskEditDialog: React.FC<{ open: boolean, onClose: () => void, task?: TaskClient.TaskDescriptor }> = (props) => {

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

export { TaskEditDialog }