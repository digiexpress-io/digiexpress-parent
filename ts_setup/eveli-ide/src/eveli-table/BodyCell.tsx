import { Typography } from '@mui/material';
import { Cell } from '@tanstack/react-table';
import React from 'react';


export interface BodyCellProps {
  cell: Cell<any, unknown>;
  children?: React.ReactNode | string;
  className: string;
}



export const BodyCell: React.FC<BodyCellProps> = ({ children, cell, className }) => {
  const width = cell.column.getSize();
  return (
    <div className={className} style={{ width }}>
      {(typeof children) === 'string' ? <Typography>{children}</Typography> : children}
    </div>
  )
}