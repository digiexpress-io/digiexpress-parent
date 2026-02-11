import React from 'react';
import { Box, styled, Tooltip } from '@mui/material';
import { TreeColors } from '../tree-theme';
import { TreeIcons } from '../tree-theme/tree-icons';
import { TreeNode, TreeNodeSecondaryView } from '../../eveli-tree-api';


export const TOOLBAR_WIDTH = '50px';

interface VerticalToolbarProps {
  isDarkMode: boolean;
  isOpen: boolean;
  onClick: () => void;
  selectedView: TreeNodeSecondaryView | undefined;
  onViewChange: (view: TreeNodeSecondaryView) => void;
  activeNode?: TreeNode;
}

export const VerticalToolbar: React.FC<VerticalToolbarProps> = ({
  isDarkMode,
  isOpen,
  onClick,
  selectedView,
  onViewChange,
  activeNode
}) => {
  const isActiveNode = !!activeNode;
  const isReference = !!activeNode?.reference;
  return (
    <ToolbarContainer isDarkMode={isDarkMode}>
      <Tooltip title={isOpen ? 'Collapse Panel' : 'Expand Panel'} placement="left">
        <StyledToolbarButton onClick={onClick} isDarkMode={isDarkMode}>
          {isOpen ? <TreeIcons.CollapseAll /> : <TreeIcons.ExpandAll />}
        </StyledToolbarButton>
      </Tooltip>

      <Tooltip title="Properties" placement="left" arrow>
        <StyledToolbarButton onClick={() => { }} disabled={!isActiveNode} isDarkMode={isDarkMode}>
          <TreeIcons.Info />
        </StyledToolbarButton>
      </Tooltip>

      <Tooltip title="Configuration" placement="left" arrow>
        <StyledToolbarButton onClick={() => { }} disabled={!isActiveNode} isDarkMode={isDarkMode}>
          <TreeIcons.Settings />
        </StyledToolbarButton>
      </Tooltip>

      <Tooltip title="References" placement="left" arrow>
        <StyledToolbarButton onClick={() => onViewChange('references')} disabled={!isActiveNode || !isReference} isSelected={isActiveNode && isReference && selectedView === 'references'} isDarkMode={isDarkMode}>
          <TreeIcons.Tree />
        </StyledToolbarButton>
      </Tooltip>

      <Tooltip title="Debug" placement="left" arrow>
        <StyledToolbarButton onClick={() => { }} disabled={!isActiveNode} isDarkMode={isDarkMode}>
          <TreeIcons.Debug />
        </StyledToolbarButton>
      </Tooltip>

      <Tooltip title="Preview" placement="left" arrow>
        <StyledToolbarButton onClick={() => { }} disabled={!isActiveNode} isDarkMode={isDarkMode}>
          <TreeIcons.Preview />
        </StyledToolbarButton>
      </Tooltip>

      <Tooltip title="History" placement="left" arrow>
        <StyledToolbarButton onClick={() => { }} disabled={!isActiveNode} isDarkMode={isDarkMode}>
          <TreeIcons.History />
        </StyledToolbarButton>
      </Tooltip>

      <Tooltip title="Help" placement="left" arrow>
        <StyledToolbarButton onClick={() => { }} disabled={!isActiveNode} isDarkMode={isDarkMode}>
          <TreeIcons.Language />
        </StyledToolbarButton>
      </Tooltip>

    </ToolbarContainer>
  );
};


interface ToolbarButtonProps {
  disabled?: boolean;
  isSelected?: boolean;
  isDarkMode: boolean;
  onClick: () => void;
  children: React.ReactNode;
}

const StyledToolbarButton: React.FC<ToolbarButtonProps> = (props) => {
  const ButtonComponent = props.isDarkMode ? StyledToolbarButtonDark : StyledToolbarButtonLight;

  return (
    <ButtonComponent
      disabled={props.disabled}
      isSelected={props.isSelected}
      onClick={props.onClick}
    >
      {props.children}
    </ButtonComponent>
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

const ButtonComponent = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'disabled' && prop !== 'isSelected'
})<{ disabled?: boolean; isSelected?: boolean }>(({ disabled }) => ({
  cursor: disabled ? 'not-allowed' : 'pointer',
  userSelect: 'none' as const,
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  opacity: disabled ? 0.3 : 1,
  borderRadius: '4px',
  padding: '4px',
  '& .MuiSvgIcon-root': {
    fontSize: '1.2rem',
  },
  '&:hover': {
    opacity: disabled ? 0.3 : 1,
  },
}));

const StyledToolbarButtonDark = styled(ButtonComponent)(({ disabled, isSelected }) => ({
  backgroundColor: isSelected ? TreeColors.semantic.primary + '26' : 'transparent',
  border: isSelected ? `1px solid ${TreeColors.semantic.primary}` : '1px solid transparent',
  '& .MuiSvgIcon-root': {
    color: isSelected ? TreeColors.semantic.primary : TreeColors.dark.text,
  },
  '&:hover': {
    backgroundColor: disabled ? 'transparent' : isSelected ? TreeColors.semantic.primary + '40' : TreeColors.dark.border + '20'
  },
}));

const StyledToolbarButtonLight = styled(ButtonComponent)(({ disabled, isSelected }) => ({
  backgroundColor: isSelected ? TreeColors.semantic.warningLight + '26' : 'transparent',
  border: isSelected ? `1px solid ${TreeColors.semantic.warningLight}` : '1px solid transparent',
  '& .MuiSvgIcon-root': {
    color: isSelected ? TreeColors.semantic.warningLight : TreeColors.light.text,
  },
  '&:hover': {
    backgroundColor: disabled ? 'transparent' : isSelected ? TreeColors.semantic.warningLight + '40' : TreeColors.light.surface
  },
}));
