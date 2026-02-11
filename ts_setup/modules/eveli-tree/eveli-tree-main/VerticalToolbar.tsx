import React from 'react';
import { Box, styled, Typography } from '@mui/material';
import { TreeColors } from '../tree-theme';


export const TOOLBAR_WIDTH = '50px';

interface VerticalToolbarProps {
  isDarkMode: boolean;
  isOpen: boolean;
  onClick: () => void;
}

export const VerticalToolbar: React.FC<VerticalToolbarProps> = ({
  isDarkMode,
  isOpen,
  onClick
}) => {
  return (
    <ToolbarContainer isDarkMode={isDarkMode}>
      <StyledToolbarButton onClick={onClick}>
        {isOpen ? 'Close' : 'Open'}
      </StyledToolbarButton>

      <StyledToolbarButton onClick={() => console.log('References clicked')}>
        Refs
      </StyledToolbarButton>

      <StyledToolbarButton onClick={() => console.log('Preview clicked')}>
        Preview
      </StyledToolbarButton>

      <StyledToolbarButton onClick={() => console.log('Help clicked')}>
        Help
      </StyledToolbarButton>

    </ToolbarContainer>
  );
};

const ToolbarContainer = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode }) => ({
  width: TOOLBAR_WIDTH,
  height: '100%',
  backgroundColor: isDarkMode ? TreeColors.dark.surface : TreeColors.light.surface,
  borderLeft: `1px solid ${isDarkMode ? TreeColors.dark.border : TreeColors.light.border}`,
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  flexShrink: 0
}));

const StyledToolbarButton = styled(Typography)(({ theme }) => ({
  padding: theme.spacing(1),
  cursor: 'pointer',
  userSelect: 'none',
  writingMode: 'vertical-rl',
  textAlign: 'center',
  ...theme.typography.caption,
  '&:hover': {
    opacity: 0.7
  },

}));