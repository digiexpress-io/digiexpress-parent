import React from 'react';
import { Box, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsPanelProps } from './FsPanelProps';
import { FsPanelRoot, useUtilityClasses } from './useUtilityClasses';




export const FsPanel: React.FC<FsPanelProps> = (props) => {
  const intl = useIntl();
  const { children, title, activeDirent, icon, noDirentMessage, secondaryChildren } = props;
  const classes = useUtilityClasses();

  return (
    <FsPanelRoot className={classes.root}>
      <div className={classes.content}>
        <div className={classes.header}>
          {icon && <Box>{icon}</Box>}
          <Typography fontWeight={500}>{title}</Typography>
        </div>

        <div className={classes.mainSection}>
          {activeDirent ? children : (
            <Typography>
              {noDirentMessage || intl.formatMessage({ id: 'fs.panel.message.selectDirent' })}
            </Typography>
          )}
        </div>

        {activeDirent && secondaryChildren && (
          <div className={classes.secondarySection}>
            {secondaryChildren}
          </div>
        )}
      </div>
    </FsPanelRoot>
  );
};

