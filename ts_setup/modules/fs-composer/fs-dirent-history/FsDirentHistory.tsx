import React from 'react';
import { Typography } from '@mui/material';
import { FsDirentHistoryProps, historyData } from './FsDirentHistoryProps';
import { useUtilityClasses, FsDirentHistoryRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';

export const FsDirentHistory: React.FC<FsDirentHistoryProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentHistoryRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>Recent changes to this item</Typography>
      <div className={classes.tableContainer}>
        <div className={classes.tableRow}>
          <div className={classes.tableHeader}>User</div>
          <div className={classes.tableHeader}>Change</div>
          <div className={classes.tableHeader}>Date</div>
        </div>
        <div className={classes.divider} />
        {historyData.map((entry, index) => (
          <div className={classes.tableRow} key={index}>
            <div className={classes.tableCell}>{entry.user}</div>
            <div className={classes.tableCell}>{entry.change}</div>
            <div className={classes.tableCell}>{entry.date}</div>
          </div>
        ))}
      </div>
    </FsDirentHistoryRoot>
  );
};