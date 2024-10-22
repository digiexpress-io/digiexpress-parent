import React from 'react';
import { TextField } from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { format } from 'date-fns';

type Props = {
  field: any
  form: any
  meta: any
  id: String
  label: String
  readonly?: boolean,
  fullWidth?: boolean,
  [x:string]: any;
}

export const Datepicker:React.FC<Props> = ({field,form:{touched, setFieldValue, setFieldError},
  label, id, readonly, fullWidth, ...other}) => {
  const dateFormat = 'dd.MM.yyyy';
  let dateValue = field.value;
  if (readonly) {
    if (!dateValue) {
      dateValue = '';
    }
    else {
      if (typeof(dateValue) === 'string') {
        dateValue = new Date(dateValue);
      }
      dateValue = format(dateValue, dateFormat);
    }
    return (
      <TextField label={label} fullWidth={fullWidth} value={dateValue} inputProps={{ readOnly : true }}
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
          onChange={date => setFieldValue(field.name, date, false)}
          {...other}
        />
  );
}