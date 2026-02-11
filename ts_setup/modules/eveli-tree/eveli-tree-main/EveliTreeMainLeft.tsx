import React from 'react';
import { Box, styled, Typography } from '@mui/material';
import { useEveliTree } from '../../eveli-tree-api';
import { TreeColors } from '../tree-theme';

export const EveliTreeMainLeft: React.FC = () => {
  const { isDarkMode, activeTabIndex, openTabs } = useEveliTree();

  const activeTab = openTabs[activeTabIndex];

  return (
    <LeftPanel isDarkMode={isDarkMode}>
      <Box p={2}>
        {activeTab ? (
          <Typography variant='subtitle2' fontWeight={500}>
            {activeTab.node.name}
          </Typography>
        ) : (
          'No asset selected'
        )}
      </Box>
    </LeftPanel>
  );
};

const LeftPanel = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode }) => ({
  flex: 1,
  height: '100%',
  backgroundColor: isDarkMode ? TreeColors.dark.background : TreeColors.light.background,
  overflow: 'auto'
}));