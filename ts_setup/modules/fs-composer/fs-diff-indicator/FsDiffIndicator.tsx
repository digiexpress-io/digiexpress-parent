import React from 'react';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDiffIndicatorProps } from './FsDiffIndicatorProps';
import { FsDiffIndicatorRoot, useUtilityClasses } from './useUtilityClasses';


export const FsDiffIndicator: React.FC<FsDiffIndicatorProps> = (props) => {
  const classes = useUtilityClasses();

  return (
    <FsDiffIndicatorRoot className={classes.root}>
      <FsIcon icon={FsIcons.Unsaved} small />
    </FsDiffIndicatorRoot>
  );
};
