import React from 'react';
import { FsMainProps } from './FsMainProps';
import { useOwnerState } from './useOwnerState';
import { FsMainRoot, useUtilityClasses } from './useUtilityClasses';
import { FsMainContent } from './FsMainContent';
import { FsMainContentPanel } from './FsMainContentPanel';
import { VerticalToolbar } from './VerticalToolbar';

export const FsMain: React.FC<FsMainProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses(props);

  return (
    <FsMainRoot ownerState={ownerState} className={classes.root}>
      <FsMainContent ownerState={ownerState} className={classes.leftPanel} />

      <div className={classes.divider} />

      <div className={`${classes.rightPanel} ${ownerState.isRightPanelOpen ? classes.rightPanelOpen : classes.rightPanelClosed}`}>
        <FsMainContentPanel ownerState={ownerState} className={classes.rightPanelContent} />
      </div>

      <VerticalToolbar ownerState={ownerState} className={classes.toolbar} />
    </FsMainRoot>
  );
};

