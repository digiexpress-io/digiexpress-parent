import React from 'react';
import { Stack, Typography } from '@mui/material';
import { DateTime } from 'luxon';


function parse(textOrDate: string | Date): DateTime {

  if (typeof textOrDate !== 'string') {
    return DateTime.fromJSDate(textOrDate);
  }

  const text = textOrDate as string;

  const isoDate = DateTime.fromISO(text);
  if(isoDate.isValid) {
    return isoDate;
  }
  const sqlDate = DateTime.fromSQL(text);
  if(sqlDate.isValid) {
    return sqlDate;
  }
  // TODO other formats
  return sqlDate;
}


export const DateTimeFormatter: React.FC<{ value: string | Date | undefined, variant?: 'text' }> = ({ value, variant }) => {
  if (value) {
    const localTime = parse(value).toLocal().setLocale('fi-FI');
    const dateStr = localTime.toLocaleString(DateTime.DATE_SHORT);
    const timeStr = localTime.toLocaleString(DateTime.TIME_SIMPLE);

    if (variant === 'text') {
      return <>{dateStr}&nbsp;{timeStr}</>
    }

    return (
      <Stack direction='column'>
        <Typography variant='body2'>{dateStr}</Typography>
        <Typography variant='body1'>{timeStr}</Typography>
      </Stack>
    );
  }
  return "-";
}