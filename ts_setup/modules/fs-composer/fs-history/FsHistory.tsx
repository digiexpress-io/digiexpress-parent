import React from 'react';
import { Typography } from '@mui/material';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsPanel } from '../fs-primitives';
import { FsHistoryProps, historyData } from './FsHistoryProps';
import { useOwnerState } from './useOwnerState';
import { FsHistoryRoot, useUtilityClasses } from './useUtilityClasses';


export const FsHistory: React.FC<FsHistoryProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  if (!props.node) {
    return (
      <FsPanel title="History" icon={<FsIcon icon={FsIcons.History} large />} activeNode={false} noNodeMessage="Select a node from the tree to view history.">
        <></>
      </FsPanel>
    );
  }

  return (
    <FsPanel title={`History: ${props.node.name}`} icon={<FsIcon icon={FsIcons.History} large />} activeNode={true}>
      <FsHistoryRoot className={classes.root} ownerState={ownerState}>
        <div className={classes.section}>
          <Typography variant="caption" className={classes.caption}>
            Recent changes to this item
          </Typography>
          {historyData.length > 0 ? (
            <div className={classes.container}>
              {historyData.map((entry, index) => (
                <div key={index} className={classes.row}>
                  <Typography className={classes.user}>{entry.user}</Typography>
                  <Typography className={classes.change}>{entry.change}</Typography>
                  <Typography className={classes.date}>{entry.date}</Typography>
                </div>
              ))}
            </div>
          ) : (
            <Typography>
              No history available for this node.
            </Typography>
          )}
        </div>
      </FsHistoryRoot>
    </FsPanel>
  );
};
