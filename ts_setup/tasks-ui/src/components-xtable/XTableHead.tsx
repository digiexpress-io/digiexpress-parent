import React from 'react';
import { TableHead as MTableHead } from '@mui/material';


export interface XTableHeadProps {
  children: React.ReactNode
}

export const XTableHead: React.FC<XTableHeadProps> = ({ children }) => {

  const rows = React.Children.map(children, (child, _childIndex) => {
    if (!React.isValidElement(child)) {
      return null;
    }
    return child
  });

  return (<MTableHead>
    {children}
  </MTableHead>);
}
