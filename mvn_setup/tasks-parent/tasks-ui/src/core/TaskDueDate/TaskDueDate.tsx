import React from 'react';
import { Button, Dialog, Typography } from '@mui/material';
import DateRangeOutlinedIcon from '@mui/icons-material/DateRangeOutlined';

import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { StaticDatePicker } from '@mui/x-date-pickers/StaticDatePicker';

import { } from 'date-fns';

import TimestampFormatter from '../TimestampFormatter';

const TaskDueDate: React.FC<{
  task: { dueDate: Date | undefined },
  onChange: (dueDate: string | undefined) => Promise<void>
}> = ({ onChange, task }) => {
  const [open, setOpen] = React.useState(false);

  function handlePickerDialog() {
    setOpen(prev => !prev);
  }

  function handleDueDateChange(datePicker: any) {
    if (datePicker) {
      const newDate: Date = datePicker;
      const backendDate = newDate.toISOString();
      onChange(backendDate).then(() => handlePickerDialog());
    } else {
      onChange(undefined).then(() => handlePickerDialog());
    }
  }

  return (
    <>
      <Button onClick={handlePickerDialog} startIcon={<DateRangeOutlinedIcon sx={{ color: 'uiElements.main', fontSize: 'small' }} />}>
        <Typography sx={{ color: 'text.primary' }}><TimestampFormatter type='date' value={task.dueDate} /></Typography>
      </Button>
      <LocalizationProvider dateAdapter={AdapterDateFns}>
        <Dialog open={open} onClose={handlePickerDialog}>
          <StaticDatePicker onAccept={handleDueDateChange} views={['month', 'day']} defaultValue={task.dueDate} />
        </Dialog>
      </LocalizationProvider>
    </>
  );
}

export default TaskDueDate;