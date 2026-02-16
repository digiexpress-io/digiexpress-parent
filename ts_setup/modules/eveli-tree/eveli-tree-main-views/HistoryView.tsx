import React from 'react';
import { Box, Typography, styled } from '@mui/material';
import { TreeNode } from '@dxs-ts/eveli-tree-api';
import { TreeColors, TreeIcons } from '../tree-theme';
import { useEveliTree } from '@dxs-ts/eveli-tree-api';
import { ViewContainer } from './ViewContainer';

export interface HistoryViewProps {
  node: TreeNode | undefined;
}

interface ItemHistoryEntry {
  user: string;
  change: string;
  date: string;
}

// Mock history data from NodeHistory.tsx
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
  const { isDarkMode } = useEveliTree();

  if (!node) {
    return (
      <ViewContainer
        title="History"
        icon={<TreeIcons.History />}
        activeNode={false}
        noNodeMessage="Select a node from the tree to view history."
      >
        <></>
      </ViewContainer>
    );
  }

  return (
    <ViewContainer
      title={`History: ${node.name}`}
      icon={<TreeIcons.History />}
      activeNode={true}
    >
      <HistorySection>
        <Typography variant="caption" sx={{ mb: 1, display: 'block' }}>
          Recent changes to this item
        </Typography>
        {historyData.length > 0 ? (
          <HistoryContainer isDarkMode={isDarkMode}>
            {historyData.map((entry, index) => (
              <HistoryRow key={index} isDarkMode={isDarkMode}>
                <HistoryUser isDarkMode={isDarkMode}>{entry.user}</HistoryUser>
                <HistoryChange isDarkMode={isDarkMode}>{entry.change}</HistoryChange>
                <HistoryDate isDarkMode={isDarkMode}>{entry.date}</HistoryDate>
              </HistoryRow>
            ))}
          </HistoryContainer>
        ) : (
          <Typography variant="body2" color="text.secondary">
            No history available for this node.
          </Typography>
        )}
      </HistorySection>
    </ViewContainer>
  );
};

const HistorySection = styled(Box)(() => ({
  marginBottom: '16px'
}));

const HistoryContainer = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode }) => ({
  display: 'flex',
  flexDirection: 'column',
  border: `1px solid ${isDarkMode ? TreeColors.dark.border : TreeColors.light.border}`,
  '& > div:nth-of-type(odd)': {
    backgroundColor: isDarkMode ? TreeColors.dark.surface : TreeColors.light.surface,
  },
}));

const HistoryRow = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode }) => ({
  display: 'flex',
  padding: '8px 12px',
  backgroundColor: isDarkMode ? TreeColors.dark.background : TreeColors.light.background,
  borderBottom: `1px solid ${isDarkMode ? TreeColors.dark.border : TreeColors.light.border}`,
  '&:last-child': {
    borderBottom: 'none'
  }
}));

const HistoryUser = styled(Typography, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode, theme }) => ({
  ...theme.typography.subtitle2,
  color: isDarkMode ? TreeColors.dark.text : TreeColors.light.text,
  flex: 1,
  fontSize: '12px',
  fontWeight: 500,
}));

const HistoryChange = styled(Typography, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode, theme }) => ({
  ...theme.typography.subtitle2,
  color: isDarkMode ? TreeColors.dark.text : TreeColors.light.text,
  flex: 2,
  fontSize: '12px',
}));

const HistoryDate = styled(Typography, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode, theme }) => ({
  ...theme.typography.subtitle2,
  color: isDarkMode ? TreeColors.dark.textSecondary : TreeColors.light.textSecondary,
  flex: 1,
  fontSize: '12px',
  textAlign: 'right',
}));