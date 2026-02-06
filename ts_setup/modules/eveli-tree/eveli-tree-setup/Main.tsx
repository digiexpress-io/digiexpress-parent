import React from 'react';
import { Box } from '@mui/material';
import { EveliTreeTabs } from '../eveli-tree-tabs';
import { useEveliTree } from '../../eveli-tree-api';
import { TreeColors } from '../tree-theme';

const Main: React.FC<{}> = () => {
  const { isDarkMode } = useEveliTree();

  return (
    <Box sx={{
      backgroundColor: isDarkMode ? TreeColors.dark.surface : TreeColors.light.background,
      color: isDarkMode ? TreeColors.dark.text : TreeColors.light.text
    }}>
      <EveliTreeTabs />
      <Box p={2}>MAIN</Box>
    </Box>)
}
export { Main }


