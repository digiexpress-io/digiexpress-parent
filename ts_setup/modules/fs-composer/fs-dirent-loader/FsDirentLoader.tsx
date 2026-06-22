import React from 'react';
import { CircularProgress } from '@mui/material';
import { FsDirentLoaderProps } from './FsDirentLoaderProps';
import { useUtilityClasses, FsDirentLoaderRoot } from './useUtilityClasses';

export const FsDirentLoader: React.FC<FsDirentLoaderProps> = (props) => {
  const classes = useUtilityClasses();

  return (
    <FsDirentLoaderRoot className={classes.root}>
      <CircularProgress />
    </FsDirentLoaderRoot>
  );
};
