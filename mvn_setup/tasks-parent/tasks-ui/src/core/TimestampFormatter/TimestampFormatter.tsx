import React from 'react';
import { useIntl } from 'react-intl';


function getFormattedDate(value: Date, locale: string) {
  const date = value?.toLocaleDateString([locale], { day: '2-digit', month: '2-digit', year: 'numeric' });
  return (<>{date}</>);
}

function getFormattedTime(value: Date, locale: string) {
  const time = value?.toLocaleTimeString([locale], { hour: '2-digit', minute: '2-digit' });
  return (<>{time}</>);
}

function getFormattedDateTime(value: Date, locale: string) {
  const date = getFormattedDate(value, locale);
  const time = getFormattedTime(value, locale);
  const dateTime = <>{date}{", "}{time}</>
  return (<>{dateTime}</>);
}

const TimestampFormatter: React.FC<{
  value: Date | undefined,
  type: 'time' | 'date' | 'dateTime',
}> = ({ value, type }) => {

  const intl = useIntl();
  const locale = intl.locale;
  console.log(locale);

  if (!value) {
    return (<>{"-"}</>);
  }
  if (type === 'date') {
    return (getFormattedDate(value, locale));
  }
  if (type === 'dateTime') {
    return (getFormattedDateTime(value, locale));
  }
  return (getFormattedTime(value, locale));
}

export default TimestampFormatter;
