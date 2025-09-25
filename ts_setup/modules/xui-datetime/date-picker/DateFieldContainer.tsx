import React from 'react';
import { Box, IconButton, useTheme } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import TodayIcon from '@mui/icons-material/Today';

import { CalendarInput, useCalendarInput } from '../calendar-input';

import { DatePickerProps } from './DatePicker';


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
  const theme = useTheme();

  const isPartiallyFilled = machine.day !== '' || machine.month !== '' || machine.year !== '';
  const showError = (!!error) || (!machine.isValid && isPartiallyFilled);

  const height = size === 'small' ? 40 : 56;

  const classicStyles = {
    height,
    border: '1px solid #ccc',
    borderRadius: 4,
    px: 1,
    width: 'fit-content' as const,
    marginInline: 'auto',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    '&:focus-within': { borderColor: 'blue' },
  };

  const muiLikeStyles = {
    height,
    border: `1px solid ${showError ? theme.palette.error.main : 'rgba(0,0,0,0.23)'}`,
    borderRadius: theme.shape.borderRadius,
    px: 1,
    width: '100%',
    boxSizing: 'border-box' as const,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    '&:focus-within': {
      borderColor: showError ? theme.palette.error.main : theme.palette.primary.main,
      boxShadow: `0 0 0 2px ${theme.palette.action.focus}`,
    },
  };

  return (
    <Box sx={variant === 'classic' ? classicStyles : muiLikeStyles}>
      <CalendarInput className="calendar-input" />
      <Box display="flex" alignItems="center" ml={0.5}>
        <IconButton size="small" onClick={onClear} aria-label="Clear">
          <CloseIcon fontSize="small" />
        </IconButton>
        <IconButton size="small" onClick={onOpen} aria-label="Open calendar">
          <TodayIcon fontSize="small" />
        </IconButton>
      </Box>
    </Box>
  );
};
