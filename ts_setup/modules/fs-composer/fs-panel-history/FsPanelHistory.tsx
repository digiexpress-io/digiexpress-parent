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

  /*
  if (!props.dirent) {
    return (
      <FsPanel title={intl.formatMessage({ id: 'fs.history.title' })}
        icon={<FsIcon icon={FsIcons.History} large />}
        activeDirent={false}
        noDirentMessage={intl.formatMessage({ id: 'fs.history.message.selectDirent' })} />
    );
  }

  const changes = props.dirent.changes;

  return (
    <FsPanel title={intl.formatMessage({ id: 'fs.history.title.direntName' }, { direntName: props.dirent.name })}
      icon={<FsIcon icon={FsIcons.History} large />}
      activeDirent={true}>
      <FsPanelHistoryRoot className={classes.root} ownerState={ownerState}>
        <div className={classes.section}>
          <Typography variant="caption" className={classes.caption}>
            {intl.formatMessage({ id: 'fs.history.sectionTitle.recentChanges' })}
          </Typography>
          {changes.length > 0 ? (
            <div className={classes.container}>
              {changes.slice().reverse().map((entry, index) => (
                <div key={index} className={classes.row}>
                  <Typography className={classes.user}>{entry.changedBy.userName}</Typography>
                  <Typography className={classes.change}>{intl.formatMessage({ id: `fs.changeType.${entry.changeType}` })}</Typography>
                  <Typography className={classes.date}>{entry.changeDate}</Typography>
                </div>
              ))}
            </div>
          ) : (
            <Typography>
                {intl.formatMessage({ id: 'fs.history.message.noHistory' })}
            </Typography>
          )}
        </div>
      </FsPanelHistoryRoot>
    </FsPanel>
  );
  */

  return <>history</>
};
