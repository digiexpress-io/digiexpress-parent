import React from 'react';
import { Stack, Typography } from '@mui/material';
import { DateTime } from 'luxon';
import { FormattedDate, FormattedTime } from 'react-intl';


export const DateTimeFormatter: React.FC<{ value: string, variant?: 'text' }> = ({ value, variant }) => {
  if (value) {
    const localTime = DateTime.fromISO(value, { zone: 'utc' }).toLocal().toJSDate();
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