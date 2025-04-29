import React from 'react';

import TimePicker from 'react-time-picker';
import ClearIcon from '@mui/icons-material/Clear';
import AccessTimeIcon from '@mui/icons-material/AccessTime';

import { useIntl } from "react-intl";
import { GInputTimeProps } from "./GInputTime";
import { InputHidden } from './InputHidden';

import { GInputTimeInput, useUtilityClasses } from './useUtilityClasses';


function parseInit(value: string | undefined) {
  if (value) {
    return value;
  }
  return null;
}


export const TimeAndCalendar: React.FC<GInputTimeProps> = (props) => {
  const intl = useIntl();
  const classes = useUtilityClasses(props.id, props.variant)
  const [value, setValue] = React.useState<string | null>(parseInit(props.value));

  const { format = 'HH:mm' } = props;

  const ownerState = {
    variant: props.variant ?? 'time',
  }

  return (
    <GInputTimeInput ownerState={ownerState} className={classes.input}>
      <InputHidden time={value} onChange={props.onChange} id={props.id} /> 

      <TimePicker 
        value={value} 
        onChange={(newValue) => setValue(newValue)} 
        format={format}
        
        clockIcon={<AccessTimeIcon />}
        clearIcon={<ClearIcon />}

        hourPlaceholder={intl.formatMessage({ id: 'gamut.forms.answer.date.placeholder.hour' })}
        minutePlaceholder={intl.formatMessage({ id: 'gamut.forms.answer.date.placeholder.minute' })}

        className='MuiInputBase-root'
      />

    </GInputTimeInput>

  );
}
