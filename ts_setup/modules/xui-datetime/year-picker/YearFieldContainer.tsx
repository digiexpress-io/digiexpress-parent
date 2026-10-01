import React from 'react';
import { Box, IconButton } from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';
import { Today as TodayIcon } from '@mui/icons-material';
import { useIntl } from 'react-intl';

import { useUtilityClasses, XuiYearPickerInput } from './useUtilityClasses';
import { YearInput } from './YearInput';
import { useYearInput } from './YearInputProvider';


export interface YearFieldContainerProps {
  size: 'small' | 'medium';
  onValidity: (isError: boolean) => void;
}

export const YearFieldContainer: React.FC<YearFieldContainerProps> = ({ size, onValidity }) => {
  const { inputValue, isError, handleInputChange, handleClear, handleOpen, disabled } = useYearInput();
  const classes = useUtilityClasses();
  const intl = useIntl();

  React.useEffect(() => {
    onValidity(isError);
  }, [isError]);

  return (
    <XuiYearPickerInput className={classes.input} ownerState={{ isError, size }}>
      <YearInput
        value={inputValue}
        disabled={disabled}
        onChange={handleInputChange}
        inputMode='numeric'
        maxLength={4}
        placeholder={intl.formatMessage({ id: 'xui.calendarInput.mask.placeholder.year', defaultMessage: 'yyyy' })}
      />
      <Box display='flex' alignItems='center' ml={0.5}>
        <IconButton size='small' onClick={handleClear} disabled={disabled} aria-label={intl.formatMessage({ id: 'xui.datetime.button.clearDate', defaultMessage: 'Clear date' })}>
          <CloseIcon fontSize='small' />
        </IconButton>
        <IconButton size='small' onClick={handleOpen} disabled={disabled} aria-label={intl.formatMessage({ id: 'xui.datetime.button.openYearPicker', defaultMessage: 'Open year picker' })}>
          <TodayIcon fontSize='small' />
        </IconButton>
      </Box>
    </XuiYearPickerInput>
  );
};
