import React from 'react';
import { Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';


export const TableHeader: React.FC<{ id: string, children?: React.ReactNode }> = ({ id, children }) => {

  return (
    <Typography variant='h1'>
      <FormattedMessage id={id}/>
      {children}
    </Typography>
  )
}