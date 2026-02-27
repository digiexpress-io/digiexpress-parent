import React from 'react';
import { Typography, styled, generateUtilityClass } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsNode } from '@dxs-ts/fs-api';
import { FsColors, FsIcons } from '../fs-theme';
import { useFs } from '@dxs-ts/fs-api';
import { ViewContainer } from './ViewContainer';

export interface HistoryViewProps {
  node: FsNode | undefined;
}

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

export const HistoryView: React.FC<HistoryViewProps> = ({ node }) => {
  const { isDarkMode } = useFs();
  const classes = useUtilityClasses(isDarkMode);

  if (!node) {
    return (
      <ViewContainer
        title="History"
        icon={<FsIcons.History />}
        activeNode={false}
        noNodeMessage="Select a node from the tree to view history."
      >
        <></>
      </ViewContainer>
    );
  }

  return (
    <ViewContainer title={`History: ${node.name}`} icon={<FsIcons.History />} activeNode={true}>
      <HistoryViewRoot className={classes.root} isDarkMode={isDarkMode}>
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
            <Typography variant="body2" color="text.secondary">
              No history available for this node.
            </Typography>
          )}
        </div>
      </HistoryViewRoot>
    </ViewContainer>
  );
};

const MUI_NAME = 'FsHistoryView';

export interface HistoryViewClasses {
  root: string;
  section: string;
  caption: string;
  container: string;
  row: string;
  user: string;
  change: string;
  date: string;
}

export type HistoryViewClassKey = keyof HistoryViewClasses;

const useUtilityClasses = (isDarkMode: boolean) => {
  const slots = {
    root: ['root'],
    section: ['section'],
    caption: ['caption'],
    container: ['container'],
    row: ['row'],
    user: ['user'],
    change: ['change'],
    date: ['date'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

const HistoryViewRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'isDarkMode',
})<{ isDarkMode: boolean }>(({ theme, isDarkMode }) => ({
  [`& .${MUI_NAME}-section`]: {
    marginBottom: theme.spacing(2),
  },

  [`& .${MUI_NAME}-caption`]: {
    marginBottom: theme.spacing(1),
    display: 'block',
  },

  [`& .${MUI_NAME}-container`]: {
    display: 'flex',
    flexDirection: 'column',
    border: `1px solid ${isDarkMode ? FsColors.dark.border : FsColors.light.border}`,

    '& > div:nth-of-type(odd)': {
      backgroundColor: isDarkMode ? FsColors.dark.surface : FsColors.light.surface,
    },
  },

  [`& .${MUI_NAME}-row`]: {
    display: 'flex',
    padding: theme.spacing(1, 1.5),
    backgroundColor: isDarkMode ? FsColors.dark.background : FsColors.light.background,
    borderBottom: `1px solid ${isDarkMode ? FsColors.dark.border : FsColors.light.border}`,

    '&:last-child': {
      borderBottom: 'none'
    }
  },

  [`& .${MUI_NAME}-user`]: {
    ...theme.typography.subtitle2,
    color: isDarkMode ? FsColors.dark.text : FsColors.light.text,
    flex: 1,
    fontSize: '12px',
    fontWeight: 500,
  },

  [`& .${MUI_NAME}-change`]: {
    ...theme.typography.subtitle2,
    color: isDarkMode ? FsColors.dark.text : FsColors.light.text,
    flex: 2,
    fontSize: '12px',
  },

  [`& .${MUI_NAME}-date`]: {
    ...theme.typography.subtitle2,
    color: isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
    flex: 1,
    fontSize: '12px',
    textAlign: 'right',
  },
}));