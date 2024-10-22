import React from 'react';
import { FormattedDate } from 'react-intl';

export const formatTime = (time:any) => {
  if (time) {
    return (
        <FormattedDate value={time} />
    )
  }
  return "-";
}