import React from 'react';
import { Box, Button, Typography } from '@mui/material';
import { Root, ColumnSlot, useUtilityClasses } from './useUtilityClasses';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';
import CheckBoxIcon from '@mui/icons-material/CheckBox';
import { Table } from '@tanstack/react-table';
import { useIntl } from 'react-intl';



export interface ToolColumnVisibilityColumnsSlotProps {
  colTitle: string;
  isVisible: boolean;
  onToggle: (newValue: boolean) => void;
}


export const ToolColumnVisibility: React.FC<{
  table: Table<any>;
  slotProps: {
    columns: ToolColumnVisibilityColumnsSlotProps[]
  }
}> = ({ table, slotProps }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();

  const resetColsDisabled = table.getState().columnVisibility ? Object.values(table.getState().columnVisibility).every(v => v === true) : true;

  return (
    <Root className={classes.root}>
      {slotProps.columns.map((delegateProps, index) => (<Visibility {...delegateProps} key={index} />))}
      <Box mt={2} />
      <Button onClick={() => table.resetColumnVisibility()} disabled={resetColsDisabled}>
        {intl.formatMessage({ id: 'eveli.table.resetColumns', defaultMessage: 'Reset columns' })}
      </Button>
    </Root>
  )
}

const Visibility: React.FC<ToolColumnVisibilityColumnsSlotProps> = ({ isVisible, colTitle, onToggle }) => {
  const classes = useUtilityClasses();
  function handleToggle() {
    onToggle(!isVisible);
  }

  return (<ColumnSlot className={classes.columnSlot} onClick={handleToggle}>
    {isVisible ?
      <CheckBoxIcon className='cols-select-checkmark-icon' /> :
      <CheckBoxOutlineBlankIcon className='cols-select-checkmark-icon' />
    }
    <Typography>{colTitle}</Typography>
  </ColumnSlot>)
}