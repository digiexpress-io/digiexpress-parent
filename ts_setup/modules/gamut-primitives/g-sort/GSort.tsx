import React from 'react';
import { Button } from '@mui/material';
import { GSortRoot, useUtilityClasses } from './useUtilityClasses';

import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward';

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