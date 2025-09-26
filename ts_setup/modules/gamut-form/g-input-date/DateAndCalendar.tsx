import React from 'react';
import { OutlinedInput } from '@mui/material';
import { DatePicker as XuiDatePicker } from '@dxs-ts/xui-datetime';
import { DateTime } from 'luxon';

import { GInputDateProps } from './GInputDate';
import { InputHidden } from './InputHidden';
import { GInputDateInput, useUtilityClasses } from './useUtilityClasses';
import { useIntl } from 'react-intl';


function parseInit(value: string | undefined) {
  if (value) {
    const result = DateTime.fromISO(value);
    return result.isValid ? result : null;
  }
  return null;
}

interface DelegateInputProps {
  setExtendedErrors: GInputDateProps['setExtendedErrors'];
  setValue: (newValue: DateTime | null) => void;
  value: DateTime | null;
}

const input = React.forwardRef<any, DelegateInputProps>((props, _ref) => {
  const intl = useIntl();
  function handleValidity(isError: boolean) {
    if (!props.setExtendedErrors) {
      return;
    }
    props.setExtendedErrors(isError ? [{
      id: "invalid-date",
      code: "invalid-date",
      description: intl.formatMessage({ id: 'xui.datetime.value.invalid', defaultMessage: 'Invalid date, check format: dd.MM.yyyy' })
    }] : [])
  }
  return (<XuiDatePicker
    fullWidth
    onValidity={handleValidity}
    value={props.value ? props.value.toJSDate() : null}
    onChange={(d) => {
      const next = d ? DateTime.fromJSDate(d) : null;
      props.setValue(next);
    }}
  />)
})

export const DateAndCalendar: React.FC<GInputDateProps> = (props) => {
  const classes = useUtilityClasses(props.id, props.variant);
  const [value, setValue] = React.useState<DateTime | null>(parseInit(props.value));
  const ownerState = { variant: props.variant ?? 'date' };
  const { setExtendedErrors } = props;

  return (
    <GInputDateInput ownerState={ownerState} className={classes.input}>
      <InputHidden dateTime={value} onChange={props.onChange} id={props.id} />
      <OutlinedInput fullWidth 
        slots={{ input }} slotProps={{ input: 
          { value, setValue, setExtendedErrors } as any
        }}/>
    </GInputDateInput>
  );
};
