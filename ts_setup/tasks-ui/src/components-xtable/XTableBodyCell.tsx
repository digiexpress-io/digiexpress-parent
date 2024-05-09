import React from 'react';
import { Box, Chip, Typography } from '@mui/material';
import { XTableCell } from './XTableCell';


const CellWithText: React.FC<{
  maxWidth?: string;
  children: React.ReactNode;
}> = ({ maxWidth, children }) => {

  return (
    <Box alignSelf="center" textOverflow="ellipsis" maxWidth={maxWidth}>
      <Typography noWrap={true} variant='body1' fontWeight="400">{children}</Typography>
    </Box>);
}


export const XTableBodyCell: React.FC<{
  width?: string;
  maxWidth?: string;
  justifyContent?: 'left' | 'right',
  children: React.ReactNode;
}> = ({ width, maxWidth, justifyContent, children }) => {

  const isString = typeof children === 'string';
  return (<XTableCell width={width} maxWidth={maxWidth}>
    <Box width={width}>
      <Box display='flex' justifyContent={justifyContent}>
        {isString ? <CellWithText children={children} maxWidth={maxWidth} /> : children }
      </Box>
    </Box>
  </XTableCell>);
}
