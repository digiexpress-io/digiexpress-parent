import React from 'react';
import { Box, Typography } from '@mui/material';
import { ColumnSlot, useUtilityClasses } from './useUtilityClasses';
import { CheckBoxOutlineBlank as CheckBoxOutlineBlankIcon } from '@mui/icons-material';
import { CheckBox as CheckBoxIcon } from '@mui/icons-material';
import { DragIndicator as DragIndicatorIcon } from '@mui/icons-material';
import { Table, Column as TableColumn } from '@tanstack/react-table';



export const Column: React.FC<{
  table: Table<any>;
  column: TableColumn<any, unknown>
}> = ({ table, column }) => {

  const classes = useUtilityClasses();
  const isVisible = column.getIsVisible();
  const colTitle = column.columnDef.header?.toString() || column.id;

  function handleToggle(e: React.MouseEvent) {
    e.stopPropagation();
    const target = table.getColumn(column?.id);
    if (target) {
      target.toggleVisibility()
    };
  }

  return (
    <ColumnSlot className={classes.columnSlot} onClick={handleToggle}>
      {isVisible ? (
        <CheckBoxIcon className='cols-select-checkmark-icon' />
      ) : (
        <CheckBoxOutlineBlankIcon className='cols-select-checkmark-icon' />
      )}
      <Typography>{colTitle}</Typography>
      <Box flex={1} />
      <Box>
        <DragIndicatorIcon sx={{ cursor: 'grab' }} color='primary' />
      </Box>
    </ColumnSlot>
  );
};
