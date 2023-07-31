import React from 'react';
import { Box, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';


const StyledLayout: React.FC<{ title?: string, children: React.ReactNode }> = ({ title, children }) => {

  return (
    <Box sx={{ width: '100%', p: 1 }}>
      {title && <Typography variant='h3'><FormattedMessage id={title} /></Typography>}
      {children}
    </Box>)
}

export default StyledLayout;
