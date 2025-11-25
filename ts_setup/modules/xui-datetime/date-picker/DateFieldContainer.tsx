import React from 'react';
import { Box, IconButton } from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';
import { Today as TodayIcon } from '@mui/icons-material';

import { CalendarInput, useCalendarInput } from '../calendar-input';

import { useUtilityClasses, XuiDateFieldInput } from './useUtilityClasses';
import { useIntl } from 'react-intl';


export interface DateFieldContainerProps {
  onClear: () => void;
  onOpen: () => void;
  onValidity: (isError: boolean) => void;
  size: 'small' | 'medium';
};

/** Internal field container that draws the border and holds the input + buttons */
export const DateFieldContainer: React.FC<DateFieldContainerProps> = ({ onClear, onOpen, size, onValidity }) => {
  const { machine } = useCalendarInput();
  const classes = useUtilityClasses();
  const intl = useIntl();

  const isBlurred: boolean = machine.focusedField === undefined || machine.focusedField === null;
  const isValidationRequired: boolean = machine.data.day !== '' || machine.data.month !== '' || machine.data.year !== '';
  const isError: boolean = (!machine.isValid && isValidationRequired);
  const isEffect = isError && isValidationRequired && isBlurred;

  React.useEffect(() => {
    onValidity(isError);
  }, [isEffect, isError])

  return (
    <XuiDateFieldInput className={classes.input} ownerState={{ isError, size  }}>
      <CalendarInput className='calendar-input' />
      <Box display='flex' alignItems='center' ml={0.5}>
        <IconButton size='small' onClick={onClear} aria-label={intl.formatMessage({ id: 'xui.datetime.button.clearDate', defaultMessage: 'Clear date' })}>
          <CloseIcon fontSize='small' />
        </IconButton>
        <IconButton size='small' onClick={onOpen} aria-label={intl.formatMessage({ id: 'xui.datetime.button.openDatePicker', defaultMessage: 'Open datepicker' })}>
          <TodayIcon fontSize='small' />
        </IconButton>
      </Box>
    </XuiDateFieldInput>
  );
};
