import React from 'react';
import { OutlinedInput, TextField } from '@mui/material';
import { DateTime } from 'luxon';
import { useIntl } from 'react-intl';
import { YearPicker } from '@dxs-ts/xui-datetime';

import { GInputDateProps } from './GInputDate';
import { InputHidden } from './InputHidden';
import { GInputDateInput, useUtilityClasses } from './useUtilityClasses';


function parseYear(value: string | undefined): number | undefined {
  if (!value) {
    return undefined;
  }
  const dt = DateTime.fromISO(value);
  return dt.isValid ? dt.year : undefined;
}


interface _YearInputProps {
  value: DateTime | null;
  disabled?: boolean;
  setDateTime: (dt: DateTime | null) => void;
  setExtendedErrors: GInputDateProps['setExtendedErrors'];
}

const _yearInput = React.forwardRef<any, _YearInputProps>((props, _ref) => {
  const intl = useIntl();
  function handleValidity(isError: boolean) {
    if (!props.setExtendedErrors) {
      return;
    }
    props.setExtendedErrors(isError ? [{
      id: 'invalid-year',
      code: 'invalid-year',
      description: intl.formatMessage({ id: 'xui.datetime.year.invalid', defaultMessage: 'Invalid year — must be between 1925 and current year + 75' })
    }] : []);
  }
  function handleChange(year: number | null) {
    props.setDateTime(year !== null ? DateTime.fromObject({ year, month: 1, day: 1 }) : null);
  }
  return (
    <YearPicker
      fullWidth
      disabled={props.disabled}
      value={props.value?.isValid ? props.value.year : null}
      onChange={handleChange}
      onValidity={handleValidity}
    />
  );
});


export const ReadOnlyYearAndCalendar: React.FC<GInputDateProps> = (props) => {
  const classes = useUtilityClasses(props.id, props.variant);
  const ownerState = { variant: props.variant ?? 'year' };
  const year = parseYear(props.value);
  const displayValue = year !== undefined ? String(year) : '--';
  return (
    <GInputDateInput ownerState={ownerState} className={classes.input}>
      <TextField fullWidth value={displayValue} slotProps={{ input: { readOnly: true } }} />
    </GInputDateInput>
  );
};


export const YearAndCalendar: React.FC<GInputDateProps> = (props) => {
  const classes = useUtilityClasses(props.id, props.variant);
  const ownerState = { variant: props.variant ?? 'year' };
  const { setExtendedErrors, disabled } = props;
  const [dateTime, setDateTime] = React.useState<DateTime | null>(() => {
    const year = parseYear(props.value);
    return year !== undefined ? DateTime.fromObject({ year, month: 1, day: 1 }) : null;
  });

  return (
    <GInputDateInput ownerState={ownerState} className={classes.input}>
      <InputHidden dateTime={dateTime} onChange={props.onChange} id={props.id} />
      <OutlinedInput fullWidth slots={{ input: _yearInput }}
        slotProps={{
          input: { value: dateTime, disabled, setDateTime, setExtendedErrors } as any
        }} />
    </GInputDateInput>
  );
};
