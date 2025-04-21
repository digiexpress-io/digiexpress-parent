import { Typography } from '@mui/material';
import React from 'react';


export const EveliTableCell: React.FC<{ children?: React.ReactNode | string, width: number }> = ({ children, width }) => {
  return (
    <div className='rowCell' style={{ width }}>
      {(typeof children) === 'string' ? <Typography>{children}</Typography> : children}
    </div>
  )
}
