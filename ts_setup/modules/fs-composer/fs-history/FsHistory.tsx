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
                {intl.formatMessage({ id: 'fs.history.message.noHistory' })}
            </Typography>
          )}
        </div>
      </FsHistoryRoot>
    </FsPanel>
  );
};

interface ItemHistoryEntry {
  user: string;
  change: string;
  date: string;
}

const historyData: ItemHistoryEntry[] = [
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