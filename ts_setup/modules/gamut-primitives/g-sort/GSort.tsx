import React from 'react';
import { Button } from '@mui/material';
import { GSortRoot, useUtilityClasses } from './useUtilityClasses';

import { ArrowDownward as ArrowDownwardIcon } from '@mui/icons-material';
import { ArrowUpward as ArrowUpwardIcon } from '@mui/icons-material';

export interface GSortProps {
  onClick: () => void;
  label: string;
  direction: string;
}

export const GSort: React.FC<GSortProps> = ({onClick, label, direction}) => {
const classes = useUtilityClasses();


  return (
    <GSortRoot className={classes.root}>
      <Button variant='contained' onClick={onClick} endIcon={direction === 'ASC' ? <ArrowUpwardIcon/> : <ArrowDownwardIcon/> }> {label}</Button>
    </GSortRoot>
  )
}