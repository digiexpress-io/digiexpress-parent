import React from 'react';
import { Box, SxProps, Typography } from '@mui/material';


const SPACING: SxProps = {
  pl: 1, py: 2
}

const NO_SPACING: SxProps = {
  pl: 0, py: 2
}

export const XPaperTitle: React.FC<{
  children: React.ReactNode, 
  variant?: 'no-spacing' | undefined
}> = ({ children, variant }) => {
  return (<Box sx={variant === 'no-spacing' ? NO_SPACING : SPACING}>{children}</Box>);
}

export const XPaperTitleTypography: React.FC<{
  children: React.ReactNode, 
  variant?: 'text-only' | undefined,
}> = ({ children, variant }) => {

  if(variant === 'text-only') {
    return (<Typography variant='h5'>{children}</Typography>);
  }
  return (<Box sx={SPACING}><Typography variant='h5'>{children}</Typography></Box>);
}