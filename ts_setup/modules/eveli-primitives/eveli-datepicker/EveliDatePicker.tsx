import React from 'react';
import { styled, TextField } from '@mui/material';
import EditCalendarIcon from '@mui/icons-material/EditCalendar';
import ClearIcon from '@mui/icons-material/Clear';

import { useLocale } from '@dxs-ts/eveli-api';
import { DateTime } from 'luxon'; 
import { useIntl } from 'react-intl';
import DatePicker from 'react-date-picker';

import 'react-date-picker/dist/DatePicker.css';



export type EveliDatePickerProps = {
  label?: string | React.ReactNode,
  readonly?: boolean,
  fullWidth?: boolean,
  value: string | Date | undefined | null;
  onChange?: (newValue: Date | null) => void;
  onKeyDown?: (event: React.KeyboardEvent<HTMLInputElement>) => void;
}


const dateFormat = 'dd.MM.yyyy';

const DatePickerStyles = styled("div")(({ theme }) => {
  return {
    '& .react-date-picker': {
      zIndex: 100,
      alignItems: 'center',
      width: '100%',
      boxSizing: 'border-box', // Prevent padding issue with fullWidth.
      padding: '4px 0 5px',
      border: '1px solid rgba(0, 0, 0, 0.23)',
      outline: '1px solid rgb(0,0,0, 0.0)',
      borderRadius: theme.spacing(0.5),

    },

    '& .react-date-picker__wrapper': {
      border: 'unset'
    },

    '& .react-date-picker__inputGroup__input': {
      ...theme.typography.body1
    },
  };
});


export const EveliDatePicker: React.FC<EveliDatePickerProps> = ({label, readonly, fullWidth, value, onChange, onKeyDown}) => {
  const dateValue: Date | null = value ? new Date(value) : null;
  const { localeForDate } = useLocale();
  const intl = useIntl();
  
  if (readonly) {    
    const date = dateValue ? DateTime.fromJSDate(dateValue).setLocale(localeForDate).toFormat(dateFormat) : '';
    if(!label) {
      return date;
    }


    return (
      <TextField label={label} fullWidth={fullWidth} value={date} 
        inputProps={{ readOnly : true }}
        InputLabelProps={{
          shrink: true,
        }}
      />
    );
  }

  return (
    <DatePickerStyles>
      <DatePicker 
        onChange={(newValue) => {
          if(!onChange) {
            return;
          }
          onChange(newValue ? DateTime.fromJSDate(newValue as Date).toJSDate() : null)
        }}
        value={dateValue} 
        calendarIcon={<EditCalendarIcon />}
        clearIcon={<ClearIcon />}
        format={dateFormat}
        className='MuiInputBase-root'
        onKeyDown={onKeyDown}
        dayPlaceholder={intl.formatMessage({ id: 'date.placeholder.day', defaultMessage: 'Day' })}
        monthPlaceholder={intl.formatMessage({ id: 'date.placeholder.month', defaultMessage: 'Month' })}
        yearPlaceholder={intl.formatMessage({ id: 'date.placeholder.year', defaultMessage: 'Year' })}
      />
  </DatePickerStyles>
  );
}