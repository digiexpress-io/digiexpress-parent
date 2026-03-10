import React from 'react';
import { FsMainProps } from './FsMainProps';
import { useOwnerState } from './useOwnerState';
import { FsMainRoot, useUtilityClasses } from './useUtilityClasses';
import { Content } from './Content';
import { ContentPanel } from './ContentPanel';
import { ContentPanelToolbar } from './ContentPanelToolbar';

export const FsMain: React.FC<FsMainProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses(props);

  return (
    <FsMainRoot ownerState={ownerState} className={classes.root}>
      <Content ownerState={ownerState} className={classes.leftPanel} children={<MainContent />} />

      <div className={classes.divider} />

      <div className={`${classes.rightPanel} ${ownerState.isRightPanelOpen ? classes.rightPanelOpen : classes.rightPanelClosed}`}>
        <ContentPanel ownerState={ownerState} className={classes.rightPanelContent} />
      </div>

      <ContentPanelToolbar ownerState={ownerState} className={classes.toolbar} />
    </FsMainRoot>
  );
};


const MainContent: React.FC = () => {

  return (<>Main content</>)
}

