import React from 'react';

import EditCalendarIcon from '@mui/icons-material/EditCalendar';
import ClearIcon from '@mui/icons-material/Clear';

import 'react-date-picker/dist/DatePicker.css';
import 'react-calendar/dist/Calendar.css';

import DatePicker from 'react-date-picker';
import { DateTime } from 'luxon';
import { useIntl } from 'react-intl';

import { GInputDateProps } from './GInputDate';
import { InputHidden } from './InputHidden';
import { GInputDateInput, useUtilityClasses } from './useUtilityClasses';



function parseInit(value: string | undefined) {
  if (value) {
    const result = DateTime.fromISO(value);
    return result.isValid ? result : null; 
  }
  return null;
}


export const DateAndCalendar: React.FC<GInputDateProps> = (props) => {
  const intl = useIntl();
  const classes = useUtilityClasses(props.id, props.variant)
  const [value, setValue] = React.useState<DateTime | null>(parseInit(props.value));
  
  const { format = 'dd.MM.yyyy' } = props;

  const ownerState = {
    variant: props.variant ?? 'date',
  }

  return (
    <GInputDateInput ownerState={ownerState} className={classes.input}>
      <InputHidden dateTime={value} onChange={props.onChange} id={props.id} /> 
      <DatePicker 
        onChange={(newValue) => setValue(newValue ? DateTime.fromJSDate(newValue as Date) : null)}
        value={value?.toJSDate()} 

        calendarIcon={<EditCalendarIcon />}
        clearIcon={<ClearIcon />}

        format={format}

        className='MuiInputBase-root'
        calendarProps={{

        }}

        dayPlaceholder={intl.formatMessage({ id: 'gamut.forms.answer.date.placeholder.day' })}
        monthPlaceholder={intl.formatMessage({ id: 'gamut.forms.answer.date.placeholder.month' })}
        yearPlaceholder={intl.formatMessage({ id: 'gamut.forms.answer.date.placeholder.year' })}
      />
    </GInputDateInput>

  );
}
