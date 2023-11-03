import React from 'react';
import { Button, Dialog, Typography } from '@mui/material';
import DateRangeOutlinedIcon from '@mui/icons-material/DateRangeOutlined';

import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { StaticDatePicker } from '@mui/x-date-pickers/StaticDatePicker';

import { } from 'date-fns';

import TimestampFormatter from 'timestamp';

import Context from 'context';
import Client from 'client';
import { TaskDescriptor } from 'taskdescriptor';

const TaskStartDate: React.FC<{
  task: TaskDescriptor,
  onChange: (command: Client.ChangeTaskStartDate) => Promise<void>
}> = ({ onChange, task }) => {

  const { state } = Context.useTaskEdit();
  const [open, setOpen] = React.useState(false);

  function handlePickerDialog() {
    setOpen(prev => !prev);
  }

  function handleStartDateChange(datePicker: any) {
    const newDate: Date = datePicker;
    const command: Client.ChangeTaskStartDate = {
      commandType: 'ChangeTaskStartDate',
      startDate: newDate.toISOString(),
      taskId: task.id
    };
    onChange(command).then(() => handlePickerDialog());
  }

  return (
    <>
      <Button onClick={handlePickerDialog} startIcon={<DateRangeOutlinedIcon sx={{ color: 'uiElements.main', fontSize: 'small' }} />}>
        <Typography sx={{ color: 'text.primary' }}><TimestampFormatter type='date' value={state.task.startDate} /></Typography>
      </Button>
      <LocalizationProvider dateAdapter={AdapterDateFns}>
        <Dialog open={open} onClose={handlePickerDialog}>
          <StaticDatePicker onAccept={handleStartDateChange} views={['month', 'day']} defaultValue={state.task.startDate} />
        </Dialog>
      </LocalizationProvider>
    </>
  );
}

export default TaskStartDate;