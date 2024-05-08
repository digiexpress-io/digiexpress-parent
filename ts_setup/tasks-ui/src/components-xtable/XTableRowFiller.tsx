import React from 'react';
import { TableRow } from '@mui/material';
import { XTableCell } from './XTableCell';
import { useXTable } from './XTableContext';


export const XTableFillerRows: React.FC<{rowsUsed: React.ReactNode}> = ({ rowsUsed }) => {
  const { columns, rows } = useXTable();
  const rowsUsedCount = React.Children.count(rowsUsed);
  const filler = rows - rowsUsedCount;

  if (filler <= 0) {
    return null;
  }

  const result: React.ReactNode[] = [];
  for (let index = 0; index < filler; index++) {
    result.push(<TableRow key={index}>
      <XTableCell colSpan={columns}></XTableCell>
    </TableRow>);
  }
  return (<>{result}</>);
}