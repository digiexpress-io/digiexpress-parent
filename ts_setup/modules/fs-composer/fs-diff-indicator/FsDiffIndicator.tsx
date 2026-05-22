import React from 'react';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDiffIndicatorProps } from './FsDiffIndicatorProps';
import { FsDiffIndicatorRoot, useUtilityClasses } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';

export const FsDiffIndicator: React.FC<FsDiffIndicatorProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDiffIndicatorRoot className={classes.root} ownerState={ownerState}>
      <FsIcon icon={FsIcons.Unsaved} small />
    </FsDiffIndicatorRoot>
  );
};
