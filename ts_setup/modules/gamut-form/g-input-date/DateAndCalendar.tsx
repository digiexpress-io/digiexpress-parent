import React from 'react';
import { OutlinedInput } from '@mui/material';
import { DatePicker as XuiDatePicker } from '@dxs-ts/xui-datetime';
import { DateTime } from 'luxon';

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
  const classes = useUtilityClasses(props.id, props.variant);
  const [value, setValue] = React.useState<DateTime | null>(parseInit(props.value));
  const ownerState = { variant: props.variant ?? 'date' };

  const input = React.forwardRef<any, {}>((itemProps, ref) => {
    return (<XuiDatePicker
      inline={false}
      popover={true}
      fullWidth
      value={value ? value.toJSDate() : null}
      onChange={(d) => {
        const next = d ? DateTime.fromJSDate(d) : null;
        setValue(next);
      }}
    />)
  })

  return (
    <GInputDateInput ownerState={ownerState} className={classes.input}>
      <InputHidden dateTime={value} onChange={props.onChange} id={props.id} />
      <OutlinedInput fullWidth slots={{ input }} />
    </GInputDateInput>
  );
};
