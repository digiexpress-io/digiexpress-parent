import React from 'react';
import { TextField } from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { format } from 'date-fns';

export type EveliDatePickerProps = {
  label: String
  readonly?: boolean,
  fullWidth?: boolean,
  value: string | Date | undefined | null;
  onChange?: (newValue: Date | null) => void
}


const dateFormat = 'dd.MM.yyyy';

export const EveliDatePicker: React.FC<EveliDatePickerProps> = ({label, readonly, fullWidth, value, onChange}) => {
  const dateValue: Date | null = value ? new Date(value) : null;
  
  if (readonly) {
    return (
      <TextField label={label} fullWidth={fullWidth} value={dateValue ? format(dateValue, dateFormat) : ''} inputProps={{ readOnly : true }}
        InputLabelProps={{
          shrink: true,
        }}
      />
    );
  }

  return (
    <DatePicker
      format={dateFormat}
      value={dateValue || null}
      label={label}
      slots={{textField: textFieldProps => <TextField fullWidth={fullWidth} {...textFieldProps} />}}
      onChange={date => {
        if(onChange) {
          onChange(date);
        }
      }}
    />
  );
}