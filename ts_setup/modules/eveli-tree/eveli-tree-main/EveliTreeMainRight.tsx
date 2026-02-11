import React from 'react';
import { Box, styled, Typography } from '@mui/material';
import { TreeColors } from '../tree-theme';
import { TreeNode, useEveliTree } from '../../eveli-tree-api';
import { ReferencesView } from './views/ReferencesView';

export interface EveliTreeMainRightProps {
  activeNode: TreeNode | undefined;
  selectedView: string | undefined;
}

export const EveliTreeMainRight: React.FC<EveliTreeMainRightProps> = ({ activeNode, selectedView }) => {
  const { isDarkMode } = useEveliTree();

  if (!activeNode) {
    return (
      <RightPanel isDarkMode={isDarkMode}>
        <Box p={2}>
          <Typography variant='subtitle2'>No Node Selected</Typography>
          <Typography variant='body2' sx={{ mt: 1, color: 'text.secondary' }}>
            Select a node from the tree to view its details.
          </Typography>
        </Box>
      </RightPanel>
    );
  }

  if (!selectedView) {
    return (
      <RightPanel isDarkMode={isDarkMode}>
        <Box p={2}>
          <Typography variant='subtitle2'>Choose a View</Typography>
          <Typography variant='body2' sx={{ mt: 1, color: 'text.secondary' }}>
            Select an option from the toolbar to view "{activeNode.name}" details.
          </Typography>
        </Box>
      </RightPanel>
    );
  }

  switch (selectedView) {
    case 'references':
      return (
        <RightPanel isDarkMode={isDarkMode}>
          <ReferencesView node={activeNode} />
        </RightPanel>
      );
    default:
      return (
        <RightPanel isDarkMode={isDarkMode}>
          <Box p={2}>
            <Typography variant='subtitle2'>View Not Implemented</Typography>
            <Typography variant='body2' sx={{ mt: 1, color: 'text.secondary' }}>
              The "{selectedView}" view is not yet implemented.
            </Typography>
          </Box>
        </RightPanel>
      );
  }
};

const RightPanel = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode }) => ({
  flex: 1,
  height: '100%',
  backgroundColor: isDarkMode ? TreeColors.dark.background : TreeColors.light.background,
  overflow: 'auto'
}));

