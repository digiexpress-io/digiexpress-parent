import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';


export const TableHeader: React.FC<{ id: string, children?: React.ReactNode }> = ({ id, children }) => {
  const intl = useIntl();

  return (
    <Typography variant='h1'>{intl.formatMessage({ id })}
      {children}
    </Typography>
  )
}