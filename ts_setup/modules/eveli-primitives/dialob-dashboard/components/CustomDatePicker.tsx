import React from 'react';
import { styled } from '@mui/material';

import EditCalendarIcon from '@mui/icons-material/EditCalendar';
import ClearIcon from '@mui/icons-material/Clear';
import { useIntl } from 'react-intl';
import DatePicker from 'react-date-picker';
import { DateTime } from 'luxon';


export interface CustomDatePickerProps {
  value: Date | undefined;
  onChange: (date: Date | null) => void;
  handleDateClear: () => void;
}

const dateFormat = 'dd.MM.yyyy';

const DatePickerStyles = styled("div")(({ theme }) => {
  return {
    '& .react-date-picker': {
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
    "& .MuiInputBase-root, MuiOutlinedInput-root": {
      height: "40px",
      paddingRight: "12px"
    }
  };
});


export const CustomDatePicker: React.FC<CustomDatePickerProps> = ({ value, onChange, handleDateClear }) => {
  const dateValue: Date | null = value ? new Date(value) : null;
  const intl = useIntl();
  
  return (
    <DatePickerStyles>
      <DatePicker 
        onChange={(newValue) => {
          if(newValue) {
            onChange(DateTime.fromJSDate(newValue as Date).toJSDate())
          } else {
            handleDateClear();
          }
        }}

        value={dateValue} 
        calendarIcon={<EditCalendarIcon />}
        clearIcon={<ClearIcon />}
        format={dateFormat}
        className='MuiInputBase-root'
        dayPlaceholder={intl.formatMessage({ id: 'date.placeholder.day', defaultMessage: 'Day' })}
        monthPlaceholder={intl.formatMessage({ id: 'date.placeholder.month', defaultMessage: 'Month' })}
        yearPlaceholder={intl.formatMessage({ id: 'date.placeholder.year', defaultMessage: 'Year' })}
      />
  </DatePickerStyles>
  );
}