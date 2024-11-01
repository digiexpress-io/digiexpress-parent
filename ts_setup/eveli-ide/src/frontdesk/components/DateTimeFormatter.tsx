import React from 'react';
import { Stack, Typography } from '@mui/material';

import moment from 'moment';
import { FormattedDate, FormattedTime } from 'react-intl';

export const DateTimeFormatter: React.FC<{ value: any }> = ({ value }) => {
  if (value) {
    const localTime = moment.utc(value).local().toDate();

    return (
      <Stack direction='column' >
        <Typography variant='body2'><FormattedDate value={localTime} /></Typography >
        <Typography variant='body1'><FormattedTime value={localTime} /></Typography >
      </Stack>
    )
  }
  return "-";
}