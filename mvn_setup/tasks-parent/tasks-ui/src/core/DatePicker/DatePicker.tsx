import React from "react";

import { Box, Button, TextField, styled } from "@mui/material";
import DateRangeIcon from '@mui/icons-material/DateRange';
import { StaticDatePicker } from '@mui/x-date-pickers/StaticDatePicker';
import { LocalizationProvider } from "@mui/x-date-pickers";
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns'

type TimeRef = 'Today' | 'Tomorrow' | 'Next week' | 'In 2 weeks';
type DateType = 'start' | 'due';

interface ShortcutItem {
  label: string;
  getValue: () => Date;
}

interface Shortcuts {
  items: ShortcutItem[];
}

interface DateChangeProps {
  value: string,
  setDate: (value: React.SetStateAction<string | Date | undefined>) => void,
  setError: (value: React.SetStateAction<string | undefined>) => void
}

interface DatePickerProps {
  startDate?: Date | string | undefined,
  setStartDate?: (value: React.SetStateAction<string | Date | undefined>) => void,
  dueDate: Date | string | undefined,
  setDueDate: (value: React.SetStateAction<string | Date | undefined>) => void,
  onClose?: () => void
}

const StyledContainer = styled(Box)(({ theme }) => ({
  '& .MuiTextField-root': {
    margin: theme.spacing(1),
    width: '25ch',
  },
  width: 'fit-content',
  padding: theme.spacing(1),
  border: '1px solid',
  borderColor: theme.palette.primary.main,
  borderRadius: theme.shape.borderRadius,
}));

function dateFormatCheck(date: string | Date | undefined): string | TimeRef {
  if (date instanceof Date) {
    switch (date.getDate()) {
      case new Date().getDate():
        return 'Today';
      case getInXDays(1).getDate():
        return 'Tomorrow';
      case getInXWeeks(1).getDate():
        return 'Next week';
      case getInXWeeks(2).getDate():
        return 'In 2 weeks';
      default:
        return date.toLocaleDateString('en-US', { year: 'numeric', month: '2-digit', day: '2-digit' });
    }
  }
  return date || '';
}

function isDateValid(date: string | undefined): boolean {
  if (date && date.length === 10 && date.match(/^\d{2}\/\d{2}\/\d{4}$/)) {
    return true;
  }
  return false;
}

function getInXDays(num: number): Date {
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1 * num);
  return tomorrow;
}

function getInXWeeks(num: number): Date {
  const nextWeek = new Date();
  nextWeek.setDate(nextWeek.getDate() + 7 * num);
  return nextWeek;
}

function getShortcuts(): Shortcuts {
  return {
    items: [
      {
        label: 'Today',
        getValue: () => new Date(),
      },
      {
        label: 'Tomorrow',
        getValue: () => getInXDays(1),
      },
      {
        label: 'Next week',
        getValue: () => getInXWeeks(1),
      },
      {
        label: 'In 2 weeks',
        getValue: () => getInXWeeks(2),
      },
    ],
  };
}

function handleDateChangeForField(args: DateChangeProps): void {
  const { value, setDate, setError } = args;
  setDate(value);
  if (isDateValid(value)) {
    setError(undefined);
  } else {
    setError('Invalid date');
  }
}

const DatePicker: React.FC<DatePickerProps> = (props) => {
  const { startDate, setStartDate, dueDate, setDueDate, onClose } = props;
  const doubleDate = setStartDate !== undefined;

  const [startDateError, setStartDateError] = React.useState<string | undefined>();
  const [dueDateError, setDueDateError] = React.useState<string | undefined>();
  const [activeField, setActiveField] = React.useState<DateType | undefined>('due');

  function handleActiveFieldChange(field: DateType | undefined): void {
    setActiveField(field);
  }

  function handleClear(): void {
    doubleDate && setStartDate(undefined);
    setStartDateError(undefined);
    setDueDate(undefined);
    setDueDateError(undefined);
    setActiveField(undefined);
  }

  function handleDateChangeForPicker(date: Date | undefined | null): void {
    date = date || undefined;
    if (activeField === 'start') {
      doubleDate && setStartDate(date);
      setStartDateError(undefined);
    }
    if (activeField === 'due') {
      setDueDate(date);
      setDueDateError(undefined);
    }
  }

  function getActiveDateForPicker(): Date | undefined {
    if (activeField === 'start' && startDate) {
      if (startDate instanceof Date) {
        return startDate;
      }
      if (isDateValid(startDate)) {
        return new Date(startDate);
      }
    }
    if (activeField === 'due' && dueDate) {
      if (dueDate instanceof Date) {
        return dueDate;
      }
      if (isDateValid(dueDate)) {
        return new Date(dueDate);
      }
    }
    return undefined;
  }

  function handleClose(e: React.MouseEvent<HTMLButtonElement, MouseEvent>): void {
    onClose && onClose();
    e.stopPropagation();
  }

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <StyledContainer>
        {doubleDate && <TextField
          id="start-date"
          type="text"
          placeholder="Start date"
          value={dateFormatCheck(startDate)}
          onChange={(e) => handleDateChangeForField({ value: e.target.value, setDate: setStartDate, setError: setStartDateError })}
          onFocus={() => handleActiveFieldChange('start')}
          InputProps={{
            startAdornment: (
              <DateRangeIcon color="primary" sx={{ mr: 1 }} />
            )
          }}
          focused={activeField === 'start'}
          helperText={startDateError}
          error={startDateError !== undefined}
          sx={{ mr: 4 }}
        />}
        <TextField
          id="due-date"
          type="text"
          placeholder="Due date"
          value={dateFormatCheck(dueDate)}
          onChange={(e) => handleDateChangeForField({ value: e.target.value, setDate: setDueDate, setError: setDueDateError })}
          onFocus={() => handleActiveFieldChange('due')}
          InputProps={{
            startAdornment: (
              <DateRangeIcon color="primary" sx={{ mr: 1 }} />
            )
          }}
          focused={activeField === 'due'}
          helperText={dueDateError}
          error={dueDateError !== undefined}
        />
        <StaticDatePicker
          displayStaticWrapperAs="desktop"
          value={getActiveDateForPicker()}
          onChange={handleDateChangeForPicker}
          sx={{ m: 1, backgroundColor: 'inherit' }}
          slotProps={{
            shortcuts: getShortcuts,
          }}
        />
        {onClose && <Button
          variant="contained"
          color="primary"
          sx={{ m: 1 }}
          onClick={(e) => handleClose(e)}
        >
          Done
        </Button>}
        <Button
          variant="text"
          color="primary"
          sx={{ m: 1 }}
          onClick={handleClear}
        >
          Clear
        </Button>
      </StyledContainer>
    </LocalizationProvider>
  );
}

export { DatePicker };