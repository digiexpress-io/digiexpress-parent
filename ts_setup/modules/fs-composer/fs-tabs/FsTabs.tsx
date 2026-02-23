import React from 'react';
import { IconButton, Tooltip, Typography } from '@mui/material';
import { FsColors, FsIcons } from '../fs-theme';
import { FsTabProps } from './FsTabProps';
import { useOwnerState } from './useOwnerState';
import { FsTabRoot, useUtilityClasses } from './useUtilityClasses';



export const FsTabs: React.FC<FsTabProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses(props);

  if (ownerState.tabs.length === 0) {
    return null;
  }


  return (
    <FsTabRoot ownerState={ownerState} className={classes.root}>
      {ownerState.tabs.map((tab, index) => (
        <div className={`${classes.tab} ${tab.isActive ? classes.active : classes.inActive}`} onClick={() => ownerState.onTabClick(index)}>
          <Tooltip title={tab.name} arrow enterDelay={700} placement="bottom">
            <Typography variant='subtitle2'
              sx={{
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
                flex: 1,
                minWidth: 0,
                color: tab.isError
                  ? (ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight)
                  : (ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text),
                fontWeight: tab.isError && ownerState.isDarkMode ? 400 : 500
              }}>{tab.name}</Typography>
          </Tooltip>
          <IconButton size="small" sx={{ ml: 0.5, p: 0.25 }} onClick={(event) => ownerState.onTabClose(index, event)}>
            <FsIcons.Close fontSize="inherit" sx={{ color: tab.isError ? (ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight) : (ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary) }} />
          </IconButton>
        </div>
      ))}
    </FsTabRoot>
  );
};



