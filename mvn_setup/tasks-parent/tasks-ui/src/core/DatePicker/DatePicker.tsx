import React from "react";

import { Box, Button, TextField, styled } from "@mui/material";
import DateRangeIcon from '@mui/icons-material/DateRange';
import { StaticDatePicker } from '@mui/x-date-pickers/StaticDatePicker';
import { LocalizationProvider } from "@mui/x-date-pickers";
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns'

type TimeRef = 'Today' | 'Tomorrow' | 'Next week' | 'In 2 weeks';
type DateType = 'start' | 'end';

interface ShortcutItem {
  label: string;
  getValue: () => Date;
}

interface Shortcuts {
  items: ShortcutItem[];
}

interface DateChangeProps {
  value: string,
  setDate: (value: React.SetStateAction<string | Date | null>) => void,
  setError: (value: React.SetStateAction<string | null>) => void
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

function dateFormatCheck(date: string | Date | null): string | TimeRef {
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

function isDateValid(date: string | null): boolean {
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
    setError(null);
  } else {
    setError('Invalid date');
  }
}

const DatePicker: React.FC<{}> = () => {

  const [startDate, setStartDate] = React.useState<Date | string | null>(null);
  const [startDateError, setStartDateError] = React.useState<string | null>(null);
  const [endDate, setEndDate] = React.useState<Date | string | null>(null);
  const [endDateError, setEndDateError] = React.useState<string | null>(null);
  const [activeField, setActiveField] = React.useState<DateType | null>(null);

  function handleActiveFieldChange(field: DateType | null): void {
    setActiveField(field);
  }

  function handleClear(): void {
    setStartDate(null);
    setStartDateError(null);
    setEndDate(null);
    setEndDateError(null);
  }

  function handleDateChangeForPicker(date: Date | null): void {
    if (activeField === 'start') {
      setStartDate(date);
      setStartDateError(null);
    }
    if (activeField === 'end') {
      setEndDate(date);
      setEndDateError(null);
    }
  }

  function getActiveDateForPicker(): Date | null {
    if (activeField === 'start' && startDate) {
      if (startDate instanceof Date) {
        return startDate;
      }
      if (isDateValid(startDate)) {
        return new Date(startDate);
      }
    }
    if (activeField === 'end' && endDate) {
      if (endDate instanceof Date) {
        return endDate;
      }
      if (isDateValid(endDate)) {
        return new Date(endDate);
      }
    }
    return null;
  }

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <StyledContainer>
        <TextField
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
          helperText={startDateError}
          error={startDateError !== null}
          sx={{ mr: 4 }}
        />
        <TextField
          id="end-date"
          type="text"
          placeholder="End date"
          value={dateFormatCheck(endDate)}
          onChange={(e) => handleDateChangeForField({ value: e.target.value, setDate: setEndDate, setError: setEndDateError })}
          onFocus={() => handleActiveFieldChange('end')}
          InputProps={{
            startAdornment: (
              <DateRangeIcon color="primary" sx={{ mr: 1 }} />
            )
          }}
          helperText={endDateError}
          error={endDateError !== null}
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