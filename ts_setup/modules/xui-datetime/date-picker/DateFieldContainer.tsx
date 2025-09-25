import React from 'react';
import { Box, IconButton } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import TodayIcon from '@mui/icons-material/Today';

import { CalendarInput, useCalendarInput } from '../calendar-input';

import { DatePickerProps } from './DatePicker';
import { useUtilityClasses, XuiDateFieldInput } from './useUtilityClasses';


export interface DateFieldContainerProps {
  onClear: () => void;
  onOpen: () => void;
  size: 'small' | 'medium';
  error?: boolean;
  variant?: DatePickerProps['variant'];
};

/** Internal field container that draws the border and holds the input + buttons */
export const DateFieldContainer: React.FC<DateFieldContainerProps> = ({ onClear, onOpen, size, error, variant = 'classic' }) => {
  const { machine } = useCalendarInput();
  const classes = useUtilityClasses();

  const isPartiallyFilled = machine.day !== '' || machine.month !== '' || machine.year !== '';
  const isError = (!!error) || (!machine.isValid && isPartiallyFilled);

  return (
    <XuiDateFieldInput className={classes.input} ownerState={{ isError, size, variant }}>
      <CalendarInput className="calendar-input" />
      <Box display="flex" alignItems="center" ml={0.5}>
        <IconButton size="small" onClick={onClear} aria-label="Clear">
          <CloseIcon fontSize="small" />
        </IconButton>
        <IconButton size="small" onClick={onOpen} aria-label="Open calendar">
          <TodayIcon fontSize="small" />
        </IconButton>
      </Box>
    </XuiDateFieldInput>
  );
};
