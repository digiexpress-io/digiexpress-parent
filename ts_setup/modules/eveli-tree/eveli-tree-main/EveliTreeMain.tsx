import React from 'react';
import { Box, styled } from '@mui/material';
import { TreeNodeSecondaryView, useEveliTree } from '../../eveli-tree-api';
import { TreeColors } from '../tree-theme';
import { EveliTreeMainLeft } from './EveliTreeMainLeft';
import { EveliTreeMainRight } from './EveliTreeMainRight';
import { VerticalToolbar, TOOLBAR_WIDTH } from './VerticalToolbar';

export const EveliTreeMain: React.FC = () => {
  const { isDarkMode, activeNode } = useEveliTree();
  const [isRightPanelOpen, setIsRightPanelOpen] = React.useState(true);
  const [selectedView, setSelectedView] = React.useState<TreeNodeSecondaryView | undefined>();

  function toggleRightPanel() {
    setIsRightPanelOpen(!isRightPanelOpen);
  };

  function handleViewChange(view: TreeNodeSecondaryView) {
    setSelectedView(view);
    if (!isRightPanelOpen) {
      setIsRightPanelOpen(true);
    }
  }


  return (
    <SplitContainer isDarkMode={isDarkMode}>
      <EveliTreeMainLeft />
      <Divider isDarkMode={isDarkMode} />
      <RightPanelContainer isOpen={isRightPanelOpen} toolbarWidth={TOOLBAR_WIDTH}>
        <EveliTreeMainRight activeNode={activeNode} selectedView={selectedView} />
      </RightPanelContainer>
      <VerticalToolbar
        isDarkMode={isDarkMode}
        isOpen={isRightPanelOpen}
        onClick={toggleRightPanel}
        selectedView={selectedView}
        onViewChange={handleViewChange}
        activeNode={activeNode}
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