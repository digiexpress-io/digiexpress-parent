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

export interface DirentHistoryEntry {
  user: string;
  change: string;
  date: string;
}

const historyData: DirentHistoryEntry[] = [
  { user: 'Diana Hasselback', change: 'Updated content', date: '15.01.2025' },
  { user: 'office-staff', change: 'Modified labels', date: '14.01.2025' },
  { user: 'John Smith', change: 'Updated description', date: '13.01.2025' },
  { user: 'Diana Hasselback', change: 'Configuration changed', date: '12.01.2025' },
  { user: 'part-time staff', change: 'Content review', date: '10.01.2025' },
  { user: 'John Smith', change: 'Updated permissions', date: '08.01.2025' },
  { user: 'office-staff', change: 'Added labels', date: '05.01.2025' },
  { user: 'Diana Hasselback', change: 'Content updated', date: '03.01.2025' },
  { user: 'John Smith', change: 'Structure modified', date: '28.12.2024' },
  { user: 'System', change: 'File created', date: '20.12.2024' }
];
