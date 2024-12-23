import React from 'react';
import { TableBody } from '@mui/material';
import { XTableFillerRows } from './XTableRowFiller';
import { XTableRowProvider } from './XTableRowContext';
import { XTableBodyProvider } from './XTableBodyContext';


export interface XTableBodyProps {
  children: React.ReactNode;
  padding?: number;
  alternate?: boolean | undefined;
}

export const XTableBody: React.FC<XTableBodyProps> = ({ children, padding, alternate }) => {

  const rows = React.Children.map(children, (child, _childIndex) => {
    if (!React.isValidElement(child)) {
      return null;
    }
    return child
  }) ?? [];

  return (
    <XTableBodyProvider padding={padding} alternate={alternate}>
      <TableBody>
        {rows.map((row, rowId) => <XTableRowProvider key={rowId} rowId={rowId}>{row}</XTableRowProvider>)}
        <XTableFillerRows rowsUsed={children} />
      </TableBody>
    </XTableBodyProvider>);
}