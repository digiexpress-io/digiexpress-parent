import Typography from '@mui/material/Typography';
import React, { PropsWithChildren } from 'react';

const style = {
  textTransform: 'uppercase',
  fontWeight: 700,
};

export const SectionTitleHeader: React.FC<PropsWithChildren> = ({ children }) => {

  return (
    <Typography variant='h6' component='h1' sx={{ ...style }}>
      {children}
    </Typography>
  );
}
