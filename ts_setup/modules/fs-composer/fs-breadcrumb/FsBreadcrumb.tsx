import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsColors, FsIcons, FsIcon } from '../fs-theme';
import { useOwnerState } from './useOwnerState';
import { FsBreadcrumbRoot, useUtilityClasses } from './useUtilityClasses';
import { FsBreadcrumbProps } from './FsBreadcrumbProps';



export const FsBreadcrumb: React.FC<FsBreadcrumbProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsBreadcrumbRoot ownerState={ownerState}>
      {ownerState.isError && (
        <FsIcon small icon={FsIcons.Error} tooltip={intl.formatMessage({ id: 'fs.breadcrumb.tooltip.error' })}
          color={ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight}
        />
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


