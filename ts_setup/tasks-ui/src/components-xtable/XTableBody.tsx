import React from 'react';
import { TableBody } from '@mui/material';
import { XTableFillerRows } from './XTableRowFiller';
import { XTableRowProvider } from './XTableRowContext';


export interface XTableBodyProps {
  children: React.ReactNode
}

export const XTableBody: React.FC<XTableBodyProps> = ({ children }) => {
  
  const rows = React.Children.map(children, (child, _childIndex) => {
    if (!React.isValidElement(child)) {
      return null;
    }
    return child
  }) ?? [];
  
  return (
    <TableBody>
      {rows.map((row, rowId) => <XTableRowProvider key={rowId} rowId={rowId}>{row}</XTableRowProvider>)}
      <XTableFillerRows rowsUsed={children} />
    </TableBody>);
}