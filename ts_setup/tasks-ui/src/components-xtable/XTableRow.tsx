import React from 'react';
import { TableRow, SxProps } from '@mui/material';
import { cyan_mud } from 'components-colors';
import { useXTableRow } from './XTableRowContext';

export interface XTableRowProps {
  children: React.ReactNode
}

function getRowBackgroundColor(index: number): string {
  const isOdd = index % 2 === 1;

  if (isOdd) {
    return cyan_mud;
  }
  return 'background.paper';
}

export const XTableRow: React.FC<XTableRowProps> = ({ children }) => {
  const { rowId, onStartHover, onEndHover } = useXTableRow();
  const backgroundColor = getRowBackgroundColor(rowId);

  return (
    <TableRow hover onMouseEnter={onStartHover} onMouseLeave={onEndHover} sx={{ backgroundColor }}>
      {children}
    </TableRow>);
}