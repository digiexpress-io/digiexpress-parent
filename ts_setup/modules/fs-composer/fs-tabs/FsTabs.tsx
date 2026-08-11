import React from 'react';
import { IconButton } from '@mui/material';
import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { FsTabProps } from './FsTabProps';
import { useOwnerState } from './useOwnerState';
import { FsTabRoot, useUtilityClasses } from './useUtilityClasses';
import { FsTab } from './FsTab';
import { FsTabMenu } from '../fs-tab-menu';



export const FsTabs: React.FC<FsTabProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses(props);
  const [menuOpen, setMenuOpen] = React.useState(false);
  const [menuTabId, setMenuTabId] = React.useState<string | undefined>(undefined);
  const [menuAnchorPosition, setMenuAnchorPosition] = React.useState<{ top: number; left: number } | undefined>(undefined);

  function handleContextMenu(event: React.MouseEvent, tabId: string) {
    event.preventDefault();
    setMenuTabId(tabId);
    setMenuAnchorPosition({ top: event.clientY, left: event.clientX });
    setMenuOpen(true);
  }

  function handleMenuClose() {
    setMenuOpen(false);
  }

  if (ownerState.tabs.length === 0) {
    return null;
  }

  return (
    <>
    <FsTabRoot ownerState={ownerState} className={classes.root}>
      {ownerState.tabs.map((tab, index) => (
        <div key={tab.tabId}
          onClick={() => ownerState.onTabClick(tab.tabId)}
          onContextMenu={(event) => handleContextMenu(event, tab.tabId)}
          className={`${classes.tab} ${tab.isActive ? classes.active : classes.inActive}
          ${tab.isError ? classes.tabError : ''}`}
        >
          <FsTab index={index} ownerState={ownerState} className={classes.tabTypography} />

          <IconButton onClick={(event) => ownerState.onTabClose(tab.tabId, event)}>
            <FsIcon icon={FsIcons.Close} large color={tab.isError ? FsColors.semantic.danger : FsColors.base.textSecondary} />
          </IconButton>
        </div>
      ))}
    </FsTabRoot>
    <FsTabMenu open={menuOpen} tabId={menuTabId} anchorPosition={menuAnchorPosition} onClose={handleMenuClose} />
    </>
  );
};





