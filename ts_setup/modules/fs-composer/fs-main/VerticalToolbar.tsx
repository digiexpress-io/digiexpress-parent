import React from 'react';
import { Box, styled, Tooltip, Badge } from '@mui/material';
import { FsColors } from '../fs-theme';
import { FsIcons } from '../fs-theme/fs-icons';
import { FsNode, FsNodeSecondaryView } from '@dxs-ts/fs-api';


export const TOOLBAR_WIDTH = '50px';

interface VerticalToolbarProps {
  isDarkMode: boolean;
  isOpen: boolean;
  onClick: () => void;
  selectedView: FsNodeSecondaryView | undefined;
  onViewChange: (view: FsNodeSecondaryView) => void;
  activeNode?: FsNode;
}

export const VerticalToolbar: React.FC<VerticalToolbarProps> = ({
  isDarkMode,
  isOpen,
  onClick,
  selectedView,
  onViewChange,
}) => {
  return (
    <ToolbarContainer isDarkMode={isDarkMode}>
      <Tooltip title={isOpen ? 'Collapse Panel' : 'Expand Panel'} placement="left">
        <StyledToolbarButton onClick={onClick} isDarkMode={isDarkMode}>
          {isOpen ? <FsIcons.CollapseAll /> : <FsIcons.ExpandAll />}
        </StyledToolbarButton>
      </Tooltip>

      <Tooltip title="Properties" placement="left" arrow>
        <StyledToolbarButton onClick={() => onViewChange('properties')} isDarkMode={isDarkMode} isSelected={selectedView === 'properties'}>
          <FsIcons.Info />
        </StyledToolbarButton>
      </Tooltip>

      <Tooltip title="Configuration" placement="left" arrow>
        <StyledToolbarButton onClick={() => onViewChange('configuration')} isDarkMode={isDarkMode} isSelected={selectedView === 'configuration'}>
          <FsIcons.Settings />
        </StyledToolbarButton>
      </Tooltip>

      <Tooltip title="References" placement="left" arrow>
        <StyledToolbarButton isDarkMode={isDarkMode}
          onClick={() => onViewChange('references')}
          isSelected={selectedView === 'references'}
        >
          <FsIcons.Tree />
        </StyledToolbarButton>
      </Tooltip>

      <Tooltip title="Debug" placement="left" arrow>
        <StyledToolbarButton onClick={() => onViewChange('debug')} isDarkMode={isDarkMode} isSelected={selectedView === 'debug'}>
          <FsIcons.Debug />
        </StyledToolbarButton>
      </Tooltip>

      <Tooltip title="Errors" placement="left" arrow>
        <StyledToolbarButton onClick={() => onViewChange('errors')} isDarkMode={isDarkMode} isSelected={selectedView === 'errors'}>
          <FsIcons.Error />
        </StyledToolbarButton>
      </Tooltip>

      <Tooltip title="Preview" placement="left" arrow>
        <StyledToolbarButton onClick={() => onViewChange('preview')} isDarkMode={isDarkMode} isSelected={selectedView === 'preview'}>
          <FsIcons.Preview />
        </StyledToolbarButton>
      </Tooltip>

      <Tooltip title="History" placement="left" arrow>
        <StyledToolbarButton onClick={() => onViewChange('history')} isDarkMode={isDarkMode} isSelected={selectedView === 'history'}>
          <FsIcons.History />
        </StyledToolbarButton>
      </Tooltip>

      <Tooltip title="Help" placement="left" arrow>
        <StyledToolbarButton onClick={() => onViewChange('help')} isDarkMode={isDarkMode} isSelected={selectedView === 'help'}>
          <FsIcons.Help />
        </StyledToolbarButton>
      </Tooltip>

      <Tooltip title="Save" placement="left" arrow>
        <StyledToolbarSaveButton onClick={() => onViewChange('changes')} isDarkMode={isDarkMode} isSelected={selectedView === 'changes'}>
          <Badge badgeContent={7} color="error"
            sx={{
              '& .MuiBadge-badge': {
                fontSize: '10px',
                fontWeight: 'bold',
                height: '16px',
                minWidth: '16px',
              }
            }}
          >
            <FsIcons.Save color='error' />
          </Badge>
        </StyledToolbarSaveButton>
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
  backgroundColor: isDarkMode ? FsColors.dark.surface : FsColors.light.surface,
  borderLeft: `1px solid ${isDarkMode ? FsColors.dark.border : FsColors.light.border}`,
  alignItems: 'center',
  flexShrink: 0
}));

const StyledToolbarButton = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'disabled' && prop !== 'isSelected' && prop !== 'isDarkMode'
})<{ disabled?: boolean; isSelected?: boolean; isDarkMode?: boolean }>(({ disabled, isSelected, isDarkMode }) => ({
  cursor: disabled ? 'not-allowed' : 'pointer',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  opacity: disabled ? 0.3 : 1,
  borderRadius: '4px',
  padding: '4px',
  backgroundColor: isSelected ?
    (isDarkMode ? FsColors.semantic.primary + '26' : FsColors.semantic.warningLight + '26') :
    'transparent',
  border: isSelected ?
    (isDarkMode ? `1px solid ${FsColors.semantic.primary}` : `1px solid ${FsColors.semantic.warningLight}`) :
    '1px solid transparent',
  '& .MuiSvgIcon-root': {
    fontSize: '1.2rem',
    color: isSelected ?
      (isDarkMode ? FsColors.semantic.primary : FsColors.semantic.warningLight) :
      (isDarkMode ? FsColors.dark.text : FsColors.light.text),
  },
  '&:hover': {
    opacity: disabled ? 0.3 : 1,
    backgroundColor: disabled ? 'transparent' :
      isSelected ?
        (isDarkMode ? FsColors.semantic.primary + '40' : FsColors.semantic.warningLight + '40') :
        (isDarkMode ? FsColors.dark.border + '20' : FsColors.light.surface)
  },
}));

const StyledToolbarSaveButton = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'disabled' && prop !== 'isSelected' && prop !== 'isDarkMode'
})<{ disabled?: boolean; isSelected?: boolean; isDarkMode?: boolean }>(({ disabled, isSelected, isDarkMode, theme }) => ({
  cursor: disabled ? 'not-allowed' : 'pointer',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  opacity: disabled ? 0.3 : 1,
  borderRadius: '4px',
  padding: '4px',
  backgroundColor: isSelected ? (isDarkMode ? FsColors.semantic.primary + '26' : FsColors.semantic.dangerLight + '26') : 'transparent',
  border: isSelected ? (isDarkMode ? `1px solid ${FsColors.semantic.dangerDark}` : `1px solid ${FsColors.semantic.dangerLight}`) : '1px solid transparent',
  '& .MuiSvgIcon-root': {
    fontSize: '1.2rem',
    color: theme.palette.error.main
  },
  '&:hover': {
    opacity: disabled ? 0.3 : 1,
    backgroundColor: disabled ? 'transparent' :
      isSelected ?
        (isDarkMode ? FsColors.semantic.primary + '40' : FsColors.semantic.warningLight + '40') :
        (isDarkMode ? FsColors.dark.border + '20' : FsColors.light.surface)
  },
}));
