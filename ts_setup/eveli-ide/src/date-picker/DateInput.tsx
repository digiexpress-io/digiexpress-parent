import React from 'react';
import { Box, IconButton, TextField, useTheme } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import TodayIcon from '@mui/icons-material/Today';
import { useIntl } from 'react-intl';

import { CalendarInput, CalendarInputProvider, useCalendarInput } from './calendar-input';
import { CalendarProvider } from './calendar-interactive';


const InputEndAdornment: React.FC<{ onClear: () => void, onOpen: () => void, disabled?: boolean }> = ({ onClear, onOpen, disabled }) => {
  return (<Box display='flex' flexDirection='row' flexGrow={1} alignItems='center' justifyContent='flex-end'>

    <IconButton size='small'
      onClick={onClear}
      disabled={disabled}
      className="p-1 hover:bg-gray-100 rounded transition-colors disabled:opacity-50"
      aria-label="Clear date"
    >
      <CloseIcon />
    </IconButton>
    
    <IconButton size='small'
      type="button"
      onClick={onOpen}
      disabled={disabled}
      className="p-1 hover:bg-gray-100 rounded transition-colors disabled:opacity-50"
      aria-label="Open calendar"
    >
      <TodayIcon />
    </IconButton>
  </Box>)
}

const getBorderPosition = (field: 'day' | 'month' | 'year') => {
  switch (field) {
    case 'day':
      return {
        left: '0.5rem',            // Start at beginning
        width: '4ch',              // Match day field width
      };
    
    case 'month':
      return {
        left: 'calc(4ch + 1rem)', // Day width + separator spacing
        width: '4ch',             // Match month field width
      };
    
    case 'year':
      return {
        left: 'calc(8ch + 1.2rem)', // Day + month + 2 separators
        width: '6ch',               // Match year field width
      };
    
    default: return { left: 0, width: 0 };
  }
};

function useFocusField(): object | undefined {
  const { machine } = useCalendarInput();
  const theme = useTheme();

  if(!machine.focusedField) {
    return undefined;
  }
  const position = getBorderPosition(machine.focusedField);

  return {    
    '.MuiInputBase-root': {
      '&::before': {
        content: '""',
        position: 'absolute',
        bottom: 0,
        height: '2px',
        backgroundColor: theme.palette.primary.main,
        ...position
      }
    }
  };
}

const TextFieldSetup: React.FC<{ endAdornment: React.ReactNode }> = ({endAdornment}) => {
  const focus = useFocusField();

  return (
    <TextField
      helperText="Enter date in DD.MM.YYYY format"
      sx={focus}
      slots={{
        htmlInput: CalendarInput,
        inputLabel: () => <></>,
      }}
      slotProps={{
        input: { endAdornment }
      }}
    />
  );
}

export interface DateInputProps {
  value: Date | null;
  inline: boolean;
  onChange: (newDate: Date | null) => void;
}

export const DateInput: React.FC<DateInputProps> = (props) => {
  const { locale } = useIntl();
  const [open, setOpen] = React.useState(false);

  function handleClose() {
    setOpen(false);
  }
  const handleDateChange = (date: Date | null) => {
    setOpen(false);
    props.onChange(date)
  }

  const handleCalendarOpen = () => {
    setOpen(true);
  }
  return (
    <>
      { !open &&
      <CalendarInputProvider value={props.value} onChange={props.onChange} onCalendarOpen={handleCalendarOpen}>
        <TextFieldSetup endAdornment={<InputEndAdornment onClear={() => handleDateChange(null)} onOpen={handleCalendarOpen} />}/>
      </CalendarInputProvider>
      }
      {open && <CalendarProvider inline={props.inline} locale={locale} open={open} onClose={handleClose} value={props.value} onChange={handleDateChange} /> }
    </>
  );
}