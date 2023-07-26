import React from 'react';
import { Box, Stack, List, Dialog } from '@mui/material';

import TaskClient from '@taskclient';
import { DatePicker } from '../../DatePicker/DatePicker';
import { StyledFullScreenDialog } from './StyledFullScreenDialog';
import Fields from './EditTaskFields';
import Events from './TaskEvents';
import Burger from '@the-wrench-io/react-burger';

const Left: React.FC<{}> = () => {

  return (
    <>
      <Box />
      <Fields.Title />
      <Fields.Description />
      <Fields.Checklist />
      <Fields.Subtasks />
      <Fields.Attachments />
    </>)
}

const Right: React.FC<{}> = () => {
  const { state } = TaskClient.useTaskEdit();
  return (<List>
    {state.events.map(event => <Events event={event} />)}
  </List>);

}


const Header: React.FC<{}> = () => {

  const [datePickerOpen, setDatePickerOpen] = React.useState(false);
  const [startDate, setStartDate] = React.useState<Date | string | undefined>();
  const [endDate, setEndDate] = React.useState<Date | string | undefined>();

  return (
    <Box display='flex'>
      <Dialog open={datePickerOpen} onClose={() => setDatePickerOpen(false)}>
        <DatePicker startDate={startDate} setStartDate={setStartDate} endDate={endDate} setEndDate={setEndDate} />
      </Dialog>
      <Stack spacing={2} direction='row' minWidth={'50%'}>
        <Fields.Status />
        <Fields.Assignee />
        <Fields.Priority />
        <Fields.Options />
      </Stack>
      <Stack spacing={2} direction='row'>
        <Fields.StartDate onClick={() => setDatePickerOpen(true)} />
        <Fields.DueDate onClick={() => setDatePickerOpen(true)} dueDate='08/31/2023' />
      </Stack>
    </Box>
  )
}



const Footer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { } = TaskClient.useTaskEdit();
  return (
    <>
      <Burger.PrimaryButton label='buttons.cancel' onClick={onClose} />
    </>
  )
}


const EditTaskDialog: React.FC<{ open: boolean, onClose: () => void, task?: TaskClient.Task }> = (props) => {

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

export { EditTaskDialog }