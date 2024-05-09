import React from 'react';
import { Box, Paper, useTheme } from '@mui/material';
import { XPaperProvider } from './XPaperContext';


export interface XPaperProps {
  color?: string | undefined;
  uuid: string; 
  children: React.ReactNode
}

export const XPaper: React.FC<XPaperProps> = ({ color, children, uuid }) => {
  const theme = useTheme();
  const radius = theme.shape.borderRadius;
  return (<Paper key={uuid}>
    <Box sx={{
      width: '100%',
      height: 3,
      backgroundColor: color,
      borderTopLeftRadius: radius,
      borderTopRightRadius: radius,
    }} />    
    <XPaperProvider uuid={uuid}>
      {children}
    </XPaperProvider>
  </Paper>);
}