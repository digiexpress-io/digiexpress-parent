import React from 'react';
import { Box, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsPanelProps } from './FsPanelProps';
import { FsPanelRoot, useUtilityClasses } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';




export const FsPanel: React.FC<FsPanelProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const { children, title, activeNode, icon, noNodeMessage, secondaryChildren } = props;
  const classes = useUtilityClasses();

  return (
    <FsPanelRoot className={classes.root} ownerState={ownerState}>
      <div className={classes.content}>
        <div className={classes.header}>
          {icon && <Box>{icon}</Box>}
          <Typography fontWeight={500}>{title}</Typography>
        </div>

        <div className={classes.mainSection}>
          {activeNode ? children : (
            <Typography>
              {noNodeMessage || intl.formatMessage({ id: 'fs.panel.message.selectNode' })}
            </Typography>
          )}
        </div>

        {activeNode && secondaryChildren && (
          <div className={classes.secondarySection}>
            {secondaryChildren}
          </div>
        )}
      </div>
    </FsPanelRoot>
  );
};

