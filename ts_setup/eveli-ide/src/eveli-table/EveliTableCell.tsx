import { TaskApi } from '@/api-task';
import { Typography } from '@mui/material';
import { Column } from '@tanstack/react-table';
import React from 'react';


interface EveliTableCellProps {
  column: Column<TaskApi.Task, any>;
  children?: React.ReactNode | string;
  width: number;
}



export const EveliTableCell: React.FC<EveliTableCellProps> = ({ children, width }) => {
  return (
    <div className='rowCell' style={{ width }}>
      {(typeof children) === 'string' ? <Typography>{children}</Typography> : children}
    </div>
  )
}
