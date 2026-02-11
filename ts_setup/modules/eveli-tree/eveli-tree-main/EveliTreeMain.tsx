import React from 'react';
import { Box, styled } from '@mui/material';
import { useEveliTree } from '../../eveli-tree-api';
import { TreeColors } from '../tree-theme';
import { EveliTreeMainLeft } from './EveliTreeMainLeft';
import { EveliTreeMainRight } from './EveliTreeMainRight';
import { VerticalToolbar, TOOLBAR_WIDTH } from './VerticalToolbar';

export const EveliTreeMain: React.FC = () => {
  const { isDarkMode } = useEveliTree();
  const [isRightPanelOpen, setIsRightPanelOpen] = React.useState(true);

  const toggleRightPanel = () => {
    setIsRightPanelOpen(!isRightPanelOpen);
  };

  return (
    <SplitContainer isDarkMode={isDarkMode}>
      <EveliTreeMainLeft />
      <Divider isDarkMode={isDarkMode} />
      <RightPanelContainer isOpen={isRightPanelOpen} toolbarWidth={TOOLBAR_WIDTH}>
        <EveliTreeMainRight />
      </RightPanelContainer>
      <VerticalToolbar
        isDarkMode={isDarkMode}
        isOpen={isRightPanelOpen}
        onClick={toggleRightPanel}
      />
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


const RightPanelContainer = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isOpen' && prop !== 'toolbarWidth'
})<{ isOpen: boolean, toolbarWidth: number | string }>(({ isOpen, toolbarWidth }) => ({
  height: '100%',
  width: isOpen ? `calc(50% - ${toolbarWidth})` : '0px',
  transition: 'width 300ms cubic-bezier(0.4, 0, 0.2, 1)',
  overflow: 'hidden',
  display: 'flex',
  flexShrink: 0
}));