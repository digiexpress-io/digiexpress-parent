import React from 'react';
import { IconButton } from '@mui/material';
import { FsColors, FsIcons } from '../fs-theme';
import { FsTabProps } from './FsTabProps';
import { useOwnerState } from './useOwnerState';
import { FsTabRoot, useUtilityClasses } from './useUtilityClasses';
import { FsTab } from './FsTab';



export const FsTabs: React.FC<FsTabProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses(props);

  if (ownerState.tabs.length === 0) {
    return null;
  }

  return (
    <FsTabRoot ownerState={ownerState} className={classes.root}>
      {ownerState.tabs.map((tab, index) => (
        <div key={index}
          onClick={() => ownerState.onTabClick(index)}
          className={`${classes.tab} ${tab.isActive ? classes.active : classes.inActive} 
          ${tab.isError ? classes.tabError : ''}`}
        >
          <FsTab
            index={index}
            ownerState={ownerState}
            className={classes.tabTypography}
          />
          <IconButton onClick={(event) => ownerState.onTabClose(index, event)}>
            <FsIcons.Close fontSize="inherit" sx={{ color: tab.isError ? (ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight) : (ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary) }} />
          </IconButton>
        </div>
      ))}
    </FsTabRoot>
  );
};





