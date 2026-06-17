import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsPanel } from '../fs-panel';
import { FsPanelHistoryProps } from './FsPanelHistoryProps';
import { useOwnerState } from './useOwnerState';
import { FsPanelHistoryRoot, useUtilityClasses } from './useUtilityClasses';


export const FsPanelHistory: React.FC<FsPanelHistoryProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsPanel
      title={intl.formatMessage({ id: 'fs.history.title.direntName' }, { direntName: ownerState.direntName })}
      icon={<FsIcon icon={FsIcons.History} large />}
      activeDirent={true}
    >
      <FsPanelHistoryRoot className={classes.root} ownerState={ownerState}>
        <div className={classes.container}>
          <div className={classes.row}>
            <Typography className={classes.change}>{intl.formatMessage({ id: 'fs.history.row.created' })}</Typography>
            <Typography className={classes.user}>{ownerState.createdBy}</Typography>
            <Typography className={classes.date}>{ownerState.createdAt}</Typography>
          </div>
          {ownerState.updatedAt && (
            <div className={classes.row}>
              <Typography className={classes.change}>{intl.formatMessage({ id: 'fs.history.row.updated' })}</Typography>
              <Typography className={classes.user}>{ownerState.updatedBy}</Typography>
              <Typography className={classes.date}>{ownerState.updatedAt}</Typography>
            </div>
          )}
        </div>
      </FsPanelHistoryRoot>
    </FsPanel>
  );
};
