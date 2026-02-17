import React from 'react';
import { Box, styled } from '@mui/material';
import { FsNodeSecondaryView, useFs } from '@dxs-ts/fs-api';
import { FsColors } from '../fs-theme';
import { FsMainLeft } from './FsMainLeft';
import { FsMainRight } from './FsMainRight';
import { VerticalToolbar, TOOLBAR_WIDTH } from './VerticalToolbar';

export const FsMain: React.FC = () => {
  const { isDarkMode, activeNode } = useFs();
  const [isRightPanelOpen, setIsRightPanelOpen] = React.useState(true);
  const [selectedView, setSelectedView] = React.useState<FsNodeSecondaryView | undefined>();

  function toggleRightPanel() {
    setIsRightPanelOpen(!isRightPanelOpen);
  };

  function handleViewChange(view: FsNodeSecondaryView) {
    setSelectedView(view);
    if (!isRightPanelOpen) {
      setIsRightPanelOpen(true);
    }
  }


  return (
    <SplitContainer isDarkMode={isDarkMode}>
      <FsMainLeft />
      <Divider isDarkMode={isDarkMode} />
      <RightPanelContainer isOpen={isRightPanelOpen} toolbarWidth={TOOLBAR_WIDTH}>
        <FsMainRight activeNode={activeNode} selectedView={selectedView} />
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
  backgroundColor: isDarkMode ? FsColors.dark.background : FsColors.light.background,
  color: isDarkMode ? FsColors.dark.text : FsColors.light.text
}));

const Divider = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode }) => ({
  width: '1px',
  height: '100%',
  backgroundColor: isDarkMode ? FsColors.dark.border : FsColors.light.border,
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