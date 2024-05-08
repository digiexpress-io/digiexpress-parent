import React from 'react';
import { TableRow, SxProps } from '@mui/material';
import { cyan_mud } from 'components-colors';
import { useXTableRow } from './XTableRowContext';

export interface XTableRowProps {
  children: React.ReactNode
}

function getRowBackgroundColor(index: number): SxProps {
  const isOdd = index % 2 === 1;

  if (isOdd) {
    return { backgroundColor: cyan_mud };
  }
  return { backgroundColor: 'background.paper' };
}

export const XTableRow: React.FC<XTableRowProps> = ({ children }) => {
  const { rowId, onStartHover, onEndHover } = useXTableRow();
  const sx = getRowBackgroundColor(rowId);
  
  return (
    <TableRow sx={sx} hover onMouseEnter={onStartHover} onMouseLeave={onEndHover}>
      {children}
    </TableRow>);
}