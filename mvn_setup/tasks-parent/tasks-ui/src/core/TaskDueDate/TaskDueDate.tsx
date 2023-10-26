import React from 'react';
import { Button, Dialog, Typography } from '@mui/material';
import DateRangeOutlinedIcon from '@mui/icons-material/DateRangeOutlined';

import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { StaticDatePicker } from '@mui/x-date-pickers/StaticDatePicker';

import { } from 'date-fns';

import TimestampFormatter from '../TimestampFormatter';

import Context from 'context';
import Client from 'taskclient';
import { TaskDescriptor } from 'taskdescriptor';

const TaskDueDate: React.FC<{
  task: TaskDescriptor,
  onChange: (command: Client.ChangeTaskDueDate) => Promise<void>
}> = ({ onChange, task }) => {

  const { state } = Context.useTaskEdit();
  const [open, setOpen] = React.useState(false);

  function handlePickerDialog() {
    setOpen(prev => !prev);
  }

  function handleDueDateChange(datePicker: any) {
    const newDate: Date = datePicker;
    const command: Client.ChangeTaskDueDate = {
      commandType: 'ChangeTaskDueDate',
      dueDate: newDate.toISOString(),
      taskId: task.id
    };
    onChange(command).then(() => handlePickerDialog());
  }

  return (
    <>
      <Button onClick={handlePickerDialog} startIcon={<DateRangeOutlinedIcon sx={{ color: 'uiElements.main', fontSize: 'small' }} />}>
        <Typography sx={{ color: 'text.primary' }}><TimestampFormatter type='date' value={state.task.dueDate} /></Typography>
      </Button>
      <LocalizationProvider dateAdapter={AdapterDateFns}>
        <Dialog open={open} onClose={handlePickerDialog}>
          <StaticDatePicker onAccept={handleDueDateChange} views={['month', 'day']} defaultValue={state.task.dueDate} />
        </Dialog>
      </LocalizationProvider>
    </>
  );
}

export default TaskDueDate;