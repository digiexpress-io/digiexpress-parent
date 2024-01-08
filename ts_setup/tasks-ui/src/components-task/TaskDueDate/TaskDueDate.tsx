import React from 'react';
import { Button, Dialog, Typography, PaperProps } from '@mui/material';
import DateRangeOutlinedIcon from '@mui/icons-material/DateRangeOutlined';

import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { StaticDatePicker } from '@mui/x-date-pickers/StaticDatePicker';

import Burger from 'components-burger';
import { cyan } from 'components-colors';


const TaskDueDate: React.FC<{
  task: { dueDate: Date | undefined },
  onChange: (dueDate: string | undefined) => Promise<void>
  disabled?: boolean
}> = ({ onChange, task, disabled }) => {
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
  if (disabled) {
    return (
      <Typography sx={{ color: 'text.primary' }}><Burger.DateTimeFormatter type='date' value={task.dueDate} /></Typography>
    )
  }

  return (
    <>
      <Button onClick={handlePickerDialog} sx={{ justifyContent: 'left' }} disabled={disabled} startIcon={<DateRangeOutlinedIcon sx={{
        cursor: 'pointer',
        color: cyan,
        fontSize: 'small'
      }} />
      }>
        <Typography sx={{ color: 'text.primary' }}><Burger.DateTimeFormatter type='date' value={task.dueDate} /></Typography>
      </Button>
      <LocalizationProvider dateAdapter={AdapterDateFns}>
        <Dialog open={open} onClose={handlePickerDialog}>
          {/* https://mui.com/x/react-date-pickers/custom-components/ */}
          <StaticDatePicker onAccept={handleDueDateChange} views={['month', 'day']} defaultValue={task.dueDate}
            slotProps={{
              actionBar: {
                actions: ["clear", "cancel", "accept"]
              }
            }}
          />
        </Dialog>
      </LocalizationProvider>
    </>
  );
}

export default TaskDueDate;