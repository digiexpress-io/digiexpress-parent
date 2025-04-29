import React from 'react';

import { Typography } from '@mui/material';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';
import CheckBoxIcon from '@mui/icons-material/CheckBox';

import { Root, ColumnSlot, useUtilityClasses } from './useUtilityClasses';



export interface ToolColumnVisibilityColumnsSlotProps {
  colTitle: string;
  isVisible: boolean;
  onToggle: () => void;
}


export const ToolColumnVisibility: React.FC<{ 
  slotProps: {
    columns: ToolColumnVisibilityColumnsSlotProps[]
  }
}> = ({ slotProps }) => {
  const classes = useUtilityClasses();

  return (
    <Root className={classes.root}>
      {slotProps.columns.map(( {onToggle, colTitle, isVisible}, index ) => (
        <ColumnSlot key={index} className={classes.columnSlot} onClick={onToggle}>
          {isVisible ? 
            <CheckBoxIcon className='cols-select-checkmark-icon' /> : 
            <CheckBoxOutlineBlankIcon className='cols-select-checkmark-icon' />
          }
          <Typography>{colTitle}</Typography>
        </ColumnSlot>
    ))}
    </Root>
  )
}