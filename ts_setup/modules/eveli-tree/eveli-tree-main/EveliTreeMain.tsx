import React from 'react';
import { Box, styled } from '@mui/material';
import { useEveliTree } from '../../eveli-tree-api';
import { TreeColors } from '../tree-theme';
import { EveliTreeMainLeft } from './EveliTreeMainLeft';
import { EveliTreeMainRight } from './EveliTreeMainRight';

export const EveliTreeMain: React.FC = () => {
  const { isDarkMode } = useEveliTree();

  return (
    <SplitContainer isDarkMode={isDarkMode}>
      <EveliTreeMainLeft />
      <Divider isDarkMode={isDarkMode} />
      <EveliTreeMainRight />
    </SplitContainer>
  );
};

const SplitContainer = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode }) => ({
  display: 'flex',
  height: '100%',
  width: '100%',
  backgroundColor: isDarkMode ? TreeColors.dark.background : TreeColors.light.background,
  color: isDarkMode ? TreeColors.dark.text : TreeColors.light.text
}));

const Divider = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode }) => ({
  width: '1px',
  height: '100%',
  backgroundColor: isDarkMode ? TreeColors.dark.border : TreeColors.light.border,
  flexShrink: 0
}));