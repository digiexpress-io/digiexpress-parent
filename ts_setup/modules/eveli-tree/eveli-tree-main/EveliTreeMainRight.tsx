import React from 'react';
import { Box, styled, Typography } from '@mui/material';
import { TreeColors } from '../tree-theme';
import { useEveliTree } from '../../eveli-tree-api';

export const EveliTreeMainRight: React.FC = () => {
  const { isDarkMode } = useEveliTree();

  return (
    <RightPanel isDarkMode={isDarkMode}>
      <RightContent isDarkMode={isDarkMode}>
        <Box p={2}>
          <Typography variant='subtitle2'>Right Panel Content</Typography>
          <Typography variant='body2' sx={{ mt: 1, color: 'text.secondary' }}>
            This is the right side of the main window. Add your content here.
          </Typography>
        </Box>
      </RightContent>
      <VerticalToolbar isDarkMode={isDarkMode}>
        <Box p={1}>
          <Typography variant='caption' sx={{ writingMode: 'vertical-rl', textAlign: 'center' }}>
            Toolbar
          </Typography>
        </Box>
      </VerticalToolbar>
    </RightPanel>
  );
};

const RightPanel = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode }) => ({
  flex: 1,
  height: '100%',
  display: 'flex',
  backgroundColor: isDarkMode ? TreeColors.dark.background : TreeColors.light.background,
}));

const RightContent = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode }) => ({
  flex: 1,
  height: '100%',
  backgroundColor: isDarkMode ? TreeColors.dark.background : TreeColors.light.background,
  overflow: 'auto'
}));

const VerticalToolbar = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode }) => ({
  width: '30px',
  height: '100%',
  backgroundColor: isDarkMode ? TreeColors.dark.surface : TreeColors.light.surface,
  borderLeft: `1px solid ${isDarkMode ? TreeColors.dark.border : TreeColors.light.border}`,
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  flexShrink: 0
}));

