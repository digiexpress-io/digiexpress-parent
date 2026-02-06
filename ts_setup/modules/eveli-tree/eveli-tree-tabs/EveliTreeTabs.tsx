import React from 'react';
import { Box, IconButton, styled, Tooltip, Typography } from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';
import { useEveliTree } from '../../eveli-tree-api';
import { TreeColors } from '../tree-theme';

export const EveliTreeTabs: React.FC = () => {
  const { isDarkMode, openTabs, activeTabIndex, setActiveTab, closeTab } = useEveliTree();

  const handleTabClick = (index: number) => {
    setActiveTab(index);
  };

  const handleCloseTab = (index: number, event: React.MouseEvent) => {
    event.stopPropagation();
    closeTab(index);
  };

  if (openTabs.length === 0) {
    return null;
  }

  return (
    <StyledTabContainer isDarkMode={isDarkMode}>
      {openTabs.map((tab, index) => (
        <StyledTab
          key={tab.id}
          isActive={activeTabIndex === index}
          onClick={() => handleTabClick(index)}
          isFirst={index === 0}
          isLast={index === openTabs.length - 1}
          isDarkMode={isDarkMode}
        >
          <Tooltip title={tab.name} arrow enterDelay={700} placement="bottom">
            <Typography variant='subtitle2'
              sx={{
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
                color: isDarkMode ? TreeColors.dark.text : TreeColors.light.text
              }}>{tab.name}</Typography>
          </Tooltip>
          <IconButton size="small" sx={{ ml: 0.5, p: 0.25 }} onClick={(event) => handleCloseTab(index, event)}>
            <CloseIcon
              fontSize="inherit"
              sx={{ color: isDarkMode ? TreeColors.dark.text : TreeColors.light.textSecondary }}
            />
          </IconButton>
        </StyledTab>
      ))}
    </StyledTabContainer>
  );
};

const StyledTab = styled(Box)<{
  isActive?: boolean;
  isFirst?: boolean;
  isLast?: boolean;
  isDarkMode?: boolean;
}>(({ theme, isActive, isFirst, isDarkMode }) => ({
  display: 'flex',
  alignItems: 'center',
  paddingLeft: theme.spacing(1),
  paddingRight: theme.spacing(1),
  minWidth: '10ch',
  maxWidth: '20ch',
  overflow: 'hidden',
  borderTop: `1px solid ${isDarkMode ? TreeColors.dark.border : TreeColors.light.border}`,
  borderLeft: isFirst ? `1px solid ${isDarkMode ? TreeColors.dark.border : TreeColors.light.border}` : 'none',
  borderRight: `1px solid ${isDarkMode ? TreeColors.dark.border : TreeColors.light.border}`,
  borderBottom: isActive
    ? `1px solid ${isDarkMode ? TreeColors.dark.surface : TreeColors.light.background}`
    : 'none',
  backgroundColor: isActive
    ? (isDarkMode ? TreeColors.dark.surface : TreeColors.light.background)
    : (isDarkMode ? TreeColors.dark.background : TreeColors.light.surface),
  cursor: 'pointer',
  marginBottom: isActive ? '-1px' : 0
}));

const StyledTabContainer = styled(Box)<{ isDarkMode?: boolean }>(({ theme, isDarkMode }) => ({
  height: 35,
  display: 'flex',
  borderBottom: `1px solid ${isDarkMode ? TreeColors.dark.border : TreeColors.light.border}`,
  width: '100%'
}));