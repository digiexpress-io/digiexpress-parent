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
  const isNodeReferencePresent = !!activeNode?.reference;
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
        <StyledToolbarButton isDarkMode={isDarkMode}
          onClick={() => onViewChange('references')}
          disabled={!isActiveNode || !isNodeReferencePresent}
          isSelected={selectedView === 'references'}
        >
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

const StyledToolbarButton = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'disabled' && prop !== 'isSelected' && prop !== 'isDarkMode'
})<{ disabled?: boolean; isSelected?: boolean; isDarkMode?: boolean }>(({ disabled, isSelected, isDarkMode }) => ({
  cursor: disabled ? 'not-allowed' : 'pointer',
  userSelect: 'none' as const,
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  opacity: disabled ? 0.3 : 1,
  borderRadius: '4px',
  padding: '4px',
  backgroundColor: isSelected ?
    (isDarkMode ? TreeColors.semantic.primary + '26' : TreeColors.semantic.warningLight + '26') :
    'transparent',
  border: isSelected ?
    (isDarkMode ? `1px solid ${TreeColors.semantic.primary}` : `1px solid ${TreeColors.semantic.warningLight}`) :
    '1px solid transparent',
  '& .MuiSvgIcon-root': {
    fontSize: '1.2rem',
    color: isSelected ?
      (isDarkMode ? TreeColors.semantic.primary : TreeColors.semantic.warningLight) :
      (isDarkMode ? TreeColors.dark.text : TreeColors.light.text),
  },
  '&:hover': {
    opacity: disabled ? 0.3 : 1,
    backgroundColor: disabled ? 'transparent' :
      isSelected ?
        (isDarkMode ? TreeColors.semantic.primary + '40' : TreeColors.semantic.warningLight + '40') :
        (isDarkMode ? TreeColors.dark.border + '20' : TreeColors.light.surface)
  },
}));
