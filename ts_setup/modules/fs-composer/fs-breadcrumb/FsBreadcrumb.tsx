import React from 'react';
import { Typography, Tooltip } from '@mui/material';
import { FsColors, FsIcons } from '../fs-theme';
import { useOwnerState } from './useOwnerState';
import { FsBreadcrumbRoot, useUtilityClasses } from './useUtilityClasses';
import { FsBreadcrumbProps } from './FsBreadcrumbProps';



export const FsBreadcrumb: React.FC<FsBreadcrumbProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsBreadcrumbRoot ownerState={ownerState}>
      {ownerState.isError && (
        <Tooltip title={<>An issue with this asset needs attention.</>}>
          <FsIcons.Error fontSize="small" sx={{ color: ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight }} />
        </Tooltip>
      )}

      {ownerState.assetPath && (
        <Typography className={classes.assetPath}>
          {ownerState.assetPath} /&nbsp;
        </Typography>
      )}
      <Typography className={classes.assetName}>
        {ownerState.assetName}
      </Typography>
    </FsBreadcrumbRoot>
  );
};

