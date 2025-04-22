import { Typography } from '@mui/material';
import { Cell } from '@tanstack/react-table';
import React from 'react';


interface EveliTableCellProps {
  cell: Cell<any, unknown>;
  children?: React.ReactNode | string;
}



export const EveliTableCell: React.FC<EveliTableCellProps> = ({ children, cell }) => {

  const width = cell.column.getSize();

  return (
    <div className='rowCell' style={{ width }}>
      {(typeof children) === 'string' ? <Typography>{children}</Typography> : children}
    </div>
  )
}
