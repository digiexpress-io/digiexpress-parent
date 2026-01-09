import React from 'react';
import { Stack, Typography } from '@mui/material';
import { DateTime } from 'luxon';

function parse(textOrDate: string | Date): DateTime {
  if (typeof textOrDate !== 'string') {
    return DateTime.fromJSDate(textOrDate);
  }

  const text = textOrDate as string;

  const isoDate = DateTime.fromISO(text);
  if (isoDate.isValid) {
    return isoDate;
  }
  const sqlDate = DateTime.fromSQL(text);
  if (sqlDate.isValid) {
    return sqlDate;
  }

  // TODO other formats
  return sqlDate;
}

export function formatDateForFilter(value: string | Date | undefined): string {
  if (!value) {
    return '';
  }
  const dateTime = parse(value);
  if (!dateTime.isValid) {
    return '';
  }
  return dateTime.toLocal().setLocale('fi').toFormat('d.M.yyyy').toLowerCase();
}

export const DateTimeFormatter: React.FC<{
  value: string | Date | undefined;
  withSeconds?: boolean;
  withColumns?: boolean;
}> = ({ value, withSeconds, withColumns }) => {
  if (!value) {
    return '-';
  }

  const dateTime = parse(value);
  if (!dateTime.isValid) {
    return '-';
  }

  const local = dateTime.toLocal().setLocale('fi');

  const dateText = local.toLocaleString(DateTime.DATE_SHORT);
  const timeText = withSeconds
    ? local.toLocaleString(DateTime.TIME_24_WITH_SECONDS)
    : local.toLocaleString(DateTime.TIME_24_SIMPLE);

    if (!withColumns) {
      return <span>{dateText} klo {timeText}</span>;
    }    

    return (
      <Stack direction="column">
        <Typography variant="body2">{dateText}</Typography>
        <Typography variant="body1">{timeText}</Typography>
      </Stack>
    );
    
};
