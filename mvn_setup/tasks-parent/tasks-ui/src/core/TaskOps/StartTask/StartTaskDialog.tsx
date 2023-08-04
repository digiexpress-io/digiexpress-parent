import React from 'react';
import { Box, Stack, Dialog, Button, Grid } from '@mui/material';

import TaskClient from '@taskclient';
import { DatePicker } from '../../DatePicker/DatePicker';
import { StyledFullScreenDialog } from '../StyledFullScreenDialog';
import Fields from './StartTaskFields';
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
 
  return (<></>);

}


const Header: React.FC<{}> = () => {

  const [datePickerOpen, setDatePickerOpen] = React.useState(false);
  const [startDate, setStartDate] = React.useState<Date | string | undefined>();
  const [dueDate, setDueDate] = React.useState<Date | string | undefined>();

  return (<>
    <Dialog open={datePickerOpen} onClose={() => setDatePickerOpen(false)}>
      <DatePicker startDate={startDate} setStartDate={setStartDate} dueDate={dueDate} setDueDate={setDueDate} />
    </Dialog>

    <Grid container>
      <Grid item md={6} lg={6}>
        <Stack spacing={2} direction='row'>
          <Fields.Status />
          <Fields.Assignee />
          <Fields.Priority />
          <Fields.Options />
        </Stack>
      </Grid>

      <Grid item md={6} lg={6}>
        <Stack spacing={2} direction='row'>
          <Fields.StartDate onClick={() => setDatePickerOpen(true)} />
          <Fields.DueDate onClick={() => setDatePickerOpen(true)} dueDate='08/31/2023' />
          <Box flexGrow={1} />
          <Stack direction='row' spacing={1} alignItems='center'>
            <Fields.MessageCount />
            <Fields.AttachmentCount />
            <Fields.NewItemNotification />
          </Stack>
          <Button variant='contained' color='warning'>Messages, attachments, form</Button>
        </Stack>
      </Grid>
    </Grid>
  </>
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


const StartTaskDialog: React.FC<{ open: boolean, onClose: () => void, task?: TaskClient.TaskDescriptor }> = (props) => {

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

export { StartTaskDialog }