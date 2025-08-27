import React from 'react';
import { Stack, Typography } from '@mui/material';
import { DateTime } from 'luxon';
import { FormattedDate, FormattedTime } from 'react-intl';


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


export const DateTimeFormatter: React.FC<{ value: string | Date, variant?: 'text' }> = ({ value, variant }) => {
  if (value) {
    const localTime = parse(value).toLocal().toJSDate();
    if(variant === 'text') {
      return (<>
        <FormattedDate value={localTime} />
        &nbsp;
        <FormattedTime value={localTime} />
      </>)
    }

    return (
      <Stack direction='column' >
        <Typography variant='body2'><FormattedDate value={localTime} /></Typography >
        <Typography variant='body1'><FormattedTime value={localTime} /></Typography >
      </Stack>
    )
  }
  return "-";
}