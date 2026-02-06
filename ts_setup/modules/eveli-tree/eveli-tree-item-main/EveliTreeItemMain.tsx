import React from 'react';
import { Box, styled } from '@mui/material';
import { useEveliTree } from '../../eveli-tree-api';
import { TreeColors } from '../tree-theme';

export const EveliTreeItemMain: React.FC = () => {
  const { isDarkMode, activeTabIndex, openTabs } = useEveliTree();

  const activeTab = openTabs[activeTabIndex];

  return (
    <SplitContainer isDarkMode={isDarkMode}>
      <LeftPanel isDarkMode={isDarkMode}>
        <Box p={2}>
          {activeTab ? activeTab.name : 'No asset selected'}
        </Box>
      </LeftPanel>
      <Divider isDarkMode={isDarkMode} />
      <RightPanel isDarkMode={isDarkMode}>
        <Box p={2}>
          Right Panel Content
        </Box>
      </RightPanel>
    </SplitContainer>
  );
};

const SplitContainer = styled(Box)<{ isDarkMode: boolean }>(({ isDarkMode }) => ({
  display: 'flex',
  height: '100%',
  width: '100%',
  backgroundColor: isDarkMode ? TreeColors.dark.background : TreeColors.light.background,
  color: isDarkMode ? TreeColors.dark.text : TreeColors.light.text
}));

const LeftPanel = styled(Box)<{ isDarkMode: boolean }>(({ isDarkMode }) => ({
  flex: 1,
  height: '100%',
  backgroundColor: isDarkMode ? TreeColors.dark.background : TreeColors.light.background,
  overflow: 'auto'
}));

const RightPanel = styled(Box)<{ isDarkMode: boolean }>(({ isDarkMode }) => ({
  flex: 1,
  height: '100%',
  backgroundColor: isDarkMode ? TreeColors.dark.background : TreeColors.light.background,
  overflow: 'auto'
}));

const Divider = styled(Box)<{ isDarkMode: boolean }>(({ isDarkMode }) => ({
  width: '1px',
  height: '100%',
  backgroundColor: isDarkMode ? TreeColors.dark.border : TreeColors.light.border,
  flexShrink: 0
}));