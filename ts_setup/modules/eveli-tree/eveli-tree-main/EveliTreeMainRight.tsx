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

  if (!selectedView) {
    return (
      <RightPanel isDarkMode={isDarkMode}>
        <Box p={2}>
          <Typography variant='subtitle2'>Choose a View</Typography>
          <Typography variant='body2' sx={{ mt: 1, color: 'text.secondary' }}>
            Select an option from the toolbar to view details.
          </Typography>
        </Box>
      </RightPanel>
    );
  }

  const renderNoContentMessage = (viewName: string) => (
    <RightPanel isDarkMode={isDarkMode}>
      <Box p={2}>
        <Typography variant='subtitle2'>{viewName}</Typography>
        {!activeNode ? (
          <Typography variant='body2' sx={{ mt: 1, color: 'text.secondary' }}>
            Select a node from the tree to view {viewName.toLowerCase()} details.
          </Typography>
        ) : (
          <Typography variant='body2' sx={{ mt: 1, color: 'text.secondary' }}>
            No content available for this view.
          </Typography>
        )}
      </Box>
    </RightPanel>
  );

  switch (selectedView) {
    case 'references':
      return (
        <RightPanel isDarkMode={isDarkMode}>
          {activeNode ? <ReferencesView node={activeNode} /> : renderNoContentMessage('References')}
        </RightPanel>
      );
    case 'properties':
      return renderNoContentMessage('Properties');
    case 'configuration':
      return renderNoContentMessage('Configuration');
    case 'debug':
      return renderNoContentMessage('Debug');
    case 'preview':
      return renderNoContentMessage('Preview');
    case 'history':
      return renderNoContentMessage('History');
    case 'help':
      return renderNoContentMessage('Help');
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

