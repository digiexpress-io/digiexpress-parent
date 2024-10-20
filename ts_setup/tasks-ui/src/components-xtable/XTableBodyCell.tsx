import React from 'react';
import { Box, Chip, Typography } from '@mui/material';
import { XTableCell } from './XTableCell';
import { useXTable } from './XTableContext';
import { useXPref } from './XPrefContext';


const CellWithText: React.FC<{
  maxWidth?: string;
  children: React.ReactNode;
}> = ({ maxWidth, children }) => {

  return (
    <Box alignSelf="center" textOverflow="ellipsis" maxWidth={maxWidth}>
      <Typography noWrap={true} variant='body1' fontWeight="400">{children}</Typography>
    </Box>);
}


export const XTableBodyCell: React.FC<{
  id: string;
  width?: string;
  maxWidth?: string;
  justifyContent?: 'left' | 'right',
  children: React.ReactNode;
  colSpan?: number | 'all';
  padding?: 'none' | undefined 
}> = ({ id, width: userWidth, maxWidth, justifyContent, children, colSpan: userColSpan, padding }) => {

  const { pref } = useXPref();
  const { columns, hiddenColumns } = useXTable();

  const vis = pref.getVisibility(id);
  const hidden = vis?.enabled === false || (hiddenColumns && hiddenColumns.includes(id));
  const colSpan = userColSpan === 'all' ? columns : userColSpan;
  const width = userColSpan === 'all' ? '100%': userWidth;

  if(hidden) {
    return null;
  }


  const isString = typeof children === 'string';
  return (<XTableCell colSpan={colSpan} padding={padding}>
      <Box display='flex' justifyContent={justifyContent} height="100%" alignItems="center" width={width} maxWidth={maxWidth} >
        {isString ? <CellWithText children={children} /> : children }
      </Box>
  </XTableCell>);
}
