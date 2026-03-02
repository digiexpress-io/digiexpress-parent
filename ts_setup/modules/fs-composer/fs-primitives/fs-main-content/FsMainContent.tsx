import React from 'react';
import { Box, Typography } from '@mui/material';
import { FsMainContentProps } from './FsMainContentProps';
import { FsMainContentRoot, useUtilityClasses } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';




export const FsMainContent: React.FC<FsMainContentProps> = (props) => {
  const ownerState = useOwnerState(props);
  const { children, title, activeNode, icon, noNodeMessage, secondaryChildren } = props;
  const classes = useUtilityClasses();

  return (
    <FsMainContentRoot className={classes.root} ownerState={ownerState}>
      <div className={classes.content}>
        <div className={classes.header}>
          {icon && <Box>{icon}</Box>}
          <Typography variant="body1" fontWeight={500}>{title}</Typography>
        </div>

        <div className={classes.mainSection}>
          {activeNode ? children : (
            <Typography variant="body2" color="text.secondary">
              {noNodeMessage || 'Select a node from the tree to view details.'}
            </Typography>
          )}
        </div>

        {activeNode && secondaryChildren && (
          <div className={classes.secondarySection}>
            {secondaryChildren}
          </div>
        )}
      </div>
    </FsMainContentRoot>
  );
};

