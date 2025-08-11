import React from 'react';
import { Stack, Typography } from '@mui/material';
import moment from 'moment'; // TODO: dead lib, replace with luxon

import { FormattedDate, FormattedTime } from 'react-intl';

export const EveliDateTimeFormatter: React.FC<{ value: any, variant?: 'text' }> = ({ value, variant }) => {
  if (value) {
    const localTime = moment.utc(value).local().toDate();

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