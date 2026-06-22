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
  const [menuTabIndex, setMenuTabIndex] = React.useState<number | undefined>(undefined);
  const [menuAnchorPosition, setMenuAnchorPosition] = React.useState<{ top: number; left: number } | undefined>(undefined);

  function handleContextMenu(event: React.MouseEvent, index: number) {
    event.preventDefault();
    setMenuTabIndex(index);
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
        <div key={index}
          onClick={() => ownerState.onTabClick(index)}
          onContextMenu={(event) => handleContextMenu(event, index)}
          className={`${classes.tab} ${tab.isActive ? classes.active : classes.inActive}
          ${tab.isError ? classes.tabError : ''}`}
        >
          <FsTab index={index} ownerState={ownerState} className={classes.tabTypography} />

          <IconButton onClick={(event) => ownerState.onTabClose(index, event)}>
            <FsIcon icon={FsIcons.Close} large color={tab.isError ? FsColors.semantic.danger : FsColors.base.textSecondary} />
          </IconButton>
        </div>
      ))}
    </FsTabRoot>
    <FsTabMenu open={menuOpen} tabIndex={menuTabIndex} anchorPosition={menuAnchorPosition} onClose={handleMenuClose} />
    </>
  );
};





