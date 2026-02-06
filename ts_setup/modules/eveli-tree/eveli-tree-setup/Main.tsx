import React from 'react';
import { Box } from '@mui/material';
import { EveliTreeTabs } from '../eveli-tree-tabs';
import { EveliTreeItemMain } from '../eveli-tree-item-main';
import { useEveliTree } from '../../eveli-tree-api';
import { TreeColors } from '../tree-theme';

const Main: React.FC<{}> = () => {
  const { isDarkMode } = useEveliTree();

  return (
    <Box sx={{
      display: 'flex',
      flexDirection: 'column',
      height: '100vh',
      backgroundColor: isDarkMode ? TreeColors.dark.surface : TreeColors.light.background,
      color: isDarkMode ? TreeColors.dark.text : TreeColors.light.text
    }}>
      <EveliTreeTabs />
      <Box sx={{ flex: 1, overflow: 'hidden' }}>
        <EveliTreeItemMain />
      </Box>
    </Box>)
}
export { Main }


