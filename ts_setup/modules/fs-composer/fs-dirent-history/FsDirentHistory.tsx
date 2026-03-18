import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentHistoryProps } from './FsDirentHistoryProps';
import { useUtilityClasses, FsDirentHistoryRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';

export const FsDirentHistory: React.FC<FsDirentHistoryProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentHistoryRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.direntHistory.title' })}</Typography>
      <div className={classes.tableContainer}>
        <div className={classes.tableRow}>
          <div className={classes.tableHeader}>{intl.formatMessage({ id: 'fs.direntHistory.tableHeader.user' })}</div>
          <div className={classes.tableHeader}>{intl.formatMessage({ id: 'fs.direntHistory.tableHeader.change' })}</div>
          <div className={classes.tableHeader}>{intl.formatMessage({ id: 'fs.direntHistory.tableHeader.date' })}</div>
        </div>
        <div className={classes.divider} />
        {(props.node?.changes ?? []).slice().reverse().map((entry, index) => (
          <div className={classes.tableRow} key={index}>
            <div className={classes.tableCell}>{entry.changedBy.userName}</div>
            <div className={classes.tableCell}>{intl.formatMessage({ id: `fs.changeType.${entry.changeType}` })}</div>
            <div className={classes.tableCell}>{entry.changeDate}</div>
          </div>
        ))}
      </div>
    </FsDirentHistoryRoot>
  );
};
