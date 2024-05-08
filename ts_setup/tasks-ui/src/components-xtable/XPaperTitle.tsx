import React from 'react';
import { Box, Typography } from '@mui/material';



export interface XPaperTitleProps {
  children: React.ReactNode
}

export const XPaperTitle: React.FC<XPaperTitleProps> = ({ children }) => {
  return (<Box sx={{ pl: 1, py: 2 }}>
    <Typography variant='h5'>
      {children}
    </Typography>
  </Box>
);
}