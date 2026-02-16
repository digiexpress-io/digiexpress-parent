import React from 'react';
import { Box } from '@mui/material';
import { EveliTreeTabs } from '../eveli-tree-tabs';
import { EveliTreeBreadcrumb } from '../eveli-tree-breadcrumb';
import { EveliTreeMain } from '../eveli-tree-main';
import { useEveliTree } from '@dxs-ts/eveli-tree-api';
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
      <EveliTreeBreadcrumb />
      <Box sx={{ flex: 1, overflow: 'hidden' }}>
        <EveliTreeMain />
      </Box>
    </Box>)
}
export { Main }


