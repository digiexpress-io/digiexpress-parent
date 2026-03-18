import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsPanel } from '../fs-panel';
import { FsHistoryProps } from './FsHistoryProps';
import { useOwnerState } from './useOwnerState';
import { FsHistoryRoot, useUtilityClasses } from './useUtilityClasses';


export const FsHistory: React.FC<FsHistoryProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  if (!props.node) {
    return (
      <FsPanel title={intl.formatMessage({ id: 'fs.history.title' })}
        icon={<FsIcon icon={FsIcons.History} large />}
        activeNode={false}
        noNodeMessage={intl.formatMessage({ id: 'fs.history.message.selectNode' })} />
    );
  }

  return (
    <FsPanel title={intl.formatMessage({ id: 'fs.history.title.nodeName' }, { nodeName: props.node.name })}
      icon={<FsIcon icon={FsIcons.History} large />}
      activeNode={true}>
      <FsHistoryRoot className={classes.root} ownerState={ownerState}>
        <div className={classes.section}>
          <Typography variant="caption" className={classes.caption}>
            {intl.formatMessage({ id: 'fs.history.sectionTitle.recentChanges' })}
          </Typography>
          {props.node.changes.length > 0 ? (
            <div className={classes.container}>
              {props.node.changes.slice().reverse().map((entry, index) => (
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
      </FsHistoryRoot>
    </FsPanel>
  );
};
