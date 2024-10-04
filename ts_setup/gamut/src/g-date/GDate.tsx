import React from 'react';
import { DateTime } from 'luxon';
import { useIntl } from 'react-intl';



export interface GDateProps {
  date: DateTime;
  variant: 'relative' | 'date-only' | 'date-time'
}

export const GDate: React.FC<GDateProps> = (props) => {
  const intl = useIntl();

  if (props.variant === 'relative') {
    const relative = props.date.setLocale(intl.locale).toRelativeCalendar();
    return <>{relative}</>;
  }
  if (props.variant === 'date-only') {
    const dateTime = props.date.setLocale('fi-FI').toLocaleString(DateTime.DATE_FULL);
    return <>{dateTime}</>;
  }
  return <>{props.date.setLocale('fi-FI').toLocaleString(DateTime.DATETIME_SHORT)}</>


}