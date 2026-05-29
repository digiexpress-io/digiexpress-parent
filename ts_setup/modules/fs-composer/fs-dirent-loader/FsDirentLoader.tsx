import React from 'react';
import { CircularProgress } from '@mui/material';
import { FsDirentLoaderProps } from './FsDirentLoaderProps';
import { useUtilityClasses, FsDirentLoaderRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';

export const FsDirentLoader: React.FC<FsDirentLoaderProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentLoaderRoot className={classes.root} ownerState={ownerState}>
      <CircularProgress />
    </FsDirentLoaderRoot>
  );
};
