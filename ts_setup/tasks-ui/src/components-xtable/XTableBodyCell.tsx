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
}> = ({ id, width: initWidth, maxWidth, justifyContent, children, colSpan: initColSpan }) => {

  const { pref } = useXPref();
  const { columns, hiddenColumns } = useXTable();

  const vis = pref.getVisibility(id);
  const hidden = vis?.enabled === false || (hiddenColumns && hiddenColumns.includes(id));
  const colSpan = initColSpan === 'all' ? columns : initColSpan;
  const width = initColSpan === 'all' ? '100%': initWidth;

  if(hidden) {
    return null;
  }


  const isString = typeof children === 'string';
  return (<XTableCell width={width} maxWidth={maxWidth} colSpan={colSpan}>
    <Box width={width}>
      <Box display='flex' justifyContent={justifyContent}>
        {isString ? <CellWithText children={children} maxWidth={maxWidth} /> : children }
      </Box>
    </Box>
  </XTableCell>);
}
