import React from 'react';
import { Box, styled, Typography, Tooltip } from '@mui/material';
import { TreeColors } from '../tree-theme';
import { TreeIcons } from '../tree-theme/tree-icons';


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
      <Tooltip title={isOpen ? 'Collapse Panel' : 'Expand Panel'} placement="left">
        <StyledToolbarButton onClick={onClick}>
          <TreeIcons.CollapseAll />
        </StyledToolbarButton>
      </Tooltip>

      <Tooltip title="Properties" placement="left" arrow>
        <StyledToolbarButton onClick={() => { }}>
          <TreeIcons.Assignment />
        </StyledToolbarButton>
      </Tooltip>

      <Tooltip title="Configuration" placement="left" arrow>
        <StyledToolbarButton onClick={() => { }}>
          <TreeIcons.Settings />
        </StyledToolbarButton>
      </Tooltip>

      <Tooltip title="References" placement="left" arrow>
        <StyledToolbarButton onClick={() => { }}>
          <TreeIcons.Link />
        </StyledToolbarButton>
      </Tooltip>

      <Tooltip title="Debug" placement="left" arrow>
        <StyledToolbarButton onClick={() => { }}>
          <TreeIcons.Debug />
        </StyledToolbarButton>
      </Tooltip>

      <Tooltip title="Preview" placement="left" arrow>
        <StyledToolbarButton onClick={() => { }}>
          <TreeIcons.Preview />
        </StyledToolbarButton>
      </Tooltip>

      <Tooltip title="Help" placement="left" arrow>
        <StyledToolbarButton onClick={() => { }}>
          <TreeIcons.Language />
        </StyledToolbarButton>
      </Tooltip>

    </ToolbarContainer>
  );
};

const ToolbarContainer = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode, theme }) => ({
  width: TOOLBAR_WIDTH,
  height: '100%',
  paddingTop: theme.spacing(1),
  paddingBottom: theme.spacing(1),
  display: 'flex',
  gap: theme.spacing(2),
  flexDirection: 'column',
  backgroundColor: isDarkMode ? TreeColors.dark.surface : TreeColors.light.surface,
  borderLeft: `1px solid ${isDarkMode ? TreeColors.dark.border : TreeColors.light.border}`,
  alignItems: 'center',
  flexShrink: 0
}));

const StyledToolbarButton = styled(Box)(({ theme }) => ({
  cursor: 'pointer',
  userSelect: 'none',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  '& .MuiSvgIcon-root': {
    fontSize: '1.2rem',
  },
  '&:hover': {
    opacity: 0.7
  },
}));