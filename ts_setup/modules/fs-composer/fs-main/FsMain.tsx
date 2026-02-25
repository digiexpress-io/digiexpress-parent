import React from 'react';
import { FsMainProps } from './FsMainProps';
import { useOwnerState } from './useOwnerState';
import { FsMainRoot, useUtilityClasses } from './useUtilityClasses';
import { FsMainLeft } from './FsMainLeft';
import { FsMainRight } from './FsMainRight';
import { VerticalToolbar } from './VerticalToolbar';

export const FsMain: React.FC<FsMainProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses(props);

  return (
    <FsMainRoot ownerState={ownerState} className={classes.root}>
      <FsMainLeft ownerState={ownerState} className={classes.leftPanel} />

      <div className={classes.divider} />

      <div className={`${classes.rightPanel} ${ownerState.isRightPanelOpen ? classes.rightPanelOpen : classes.rightPanelClosed}`}>
        <FsMainRight ownerState={ownerState} className={classes.rightPanelContent} />
      </div>

      <VerticalToolbar ownerState={ownerState} className={classes.toolbar} />
    </FsMainRoot>
  );
};

