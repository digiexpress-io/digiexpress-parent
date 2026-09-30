import React from 'react';
import { Box, IconButton } from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';
import { Today as TodayIcon } from '@mui/icons-material';
import { useIntl } from 'react-intl';

import { useUtilityClasses, XuiYearPickerInput } from './useUtilityClasses';
import { YearInput } from './YearInput';


export interface YearFieldContainerProps {
  value: string;
  isError: boolean;
  disabled?: boolean;
  size: 'small' | 'medium';
  onChange: (event: React.ChangeEvent<HTMLInputElement>) => void;
  onClear: () => void;
  onOpen: () => void;
}

export const YearFieldContainer: React.FC<YearFieldContainerProps> = ({ value, isError, disabled, size, onChange, onClear, onOpen }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();

  return (
    <XuiYearPickerInput className={classes.input} ownerState={{ isError, size }}>
      <YearInput
        value={value}
        disabled={disabled}
        onChange={onChange}
        inputMode='numeric'
        maxLength={4}
      />
      <Box display='flex' alignItems='center' ml={0.5}>
        <IconButton size='small' onClick={onClear} disabled={disabled} aria-label={intl.formatMessage({ id: 'xui.datetime.button.clearDate', defaultMessage: 'Clear date' })}>
          <CloseIcon fontSize='small' />
        </IconButton>
        <IconButton size='small' onClick={onOpen} disabled={disabled} aria-label={intl.formatMessage({ id: 'xui.datetime.button.openYearPicker', defaultMessage: 'Open year picker' })}>
          <TodayIcon fontSize='small' />
        </IconButton>
      </Box>
    </XuiYearPickerInput>
  );
};
