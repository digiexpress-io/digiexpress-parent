import React from 'react';
import { TableRow } from '@mui/material';
import { cyan_mud } from 'components-colors';
import { useXTableRow } from './XTableRowContext';
import { useXTableBody } from './XTableBodyContext';

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
  const { alternate } = useXTableBody();
  const { rowId, onStartHover, onEndHover } = useXTableRow();
  const backgroundColor = alternate ? getRowBackgroundColor(rowId) : undefined;

  return (
    <TableRow hover onMouseEnter={onStartHover} onMouseLeave={onEndHover} sx={{ backgroundColor }}>
      {children}
    </TableRow>);
}