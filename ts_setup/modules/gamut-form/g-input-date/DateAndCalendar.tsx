import React from 'react';

//import { DatePicker as XuiDatePicker } from '@dxs-ts/xui-datetime'; //TODO
import { DatePicker as XuiDatePicker } from '../../xui-datetime/date-picker';

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
  const classes = useUtilityClasses(props.id, props.variant);

  const [value, setValue] = React.useState<DateTime | null>(parseInit(props.value));
  const { format = 'dd.MM.yyyy' } = props;

  const ownerState = { variant: props.variant ?? 'date' };

  const toIsoNoOffset = (dt: DateTime | null): string | undefined => {
    if (!dt) return undefined;
    const local = dt.toLocal();
    return local.toISO({ includeOffset: false }) ?? undefined;
  };

  return (
    <GInputDateInput ownerState={ownerState} className={classes.input}>
      <InputHidden dateTime={value} onChange={props.onChange} id={props.id} />

      <XuiDatePicker
        variant="mui-like"
        fullWidth
        size="small"
        value={value ? value.toJSDate() : null}
        onChange={(d) => {
          const next = d ? DateTime.fromJSDate(d) : null;
          setValue(next);
        }}
      />
    </GInputDateInput>
  );
};
