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

  const TabContainer = isDarkMode ? StyledTabContainerDark : StyledTabContainerLight;
  const Tab = isDarkMode ? StyledTabDark : StyledTabLight;

  return (
    <TabContainer>
      {openTabs.map((tab, index) => (
        <Tab
          key={tab.id}
          isActive={activeTabIndex === index}
          onClick={() => handleTabClick(index)}
          isFirst={index === 0}
          isLast={index === openTabs.length - 1}
        >
          <Tooltip title={tab.name} arrow enterDelay={700} placement="bottom">
            <Typography variant='subtitle2'
              sx={{
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
                flex: 1,
                minWidth: 0,
                color: isDarkMode ? TreeColors.dark.text : TreeColors.light.text
              }}>{tab.name}</Typography>
          </Tooltip>
          <IconButton size="small" sx={{ ml: 0.5, p: 0.25 }} onClick={(event) => handleCloseTab(index, event)}>
            <CloseIcon
              fontSize="inherit"
              sx={{ color: isDarkMode ? TreeColors.dark.text : TreeColors.light.textSecondary }}
            />
          </IconButton>
        </Tab>
      ))}
    </TabContainer>
  );
};


interface TabProps {
  isActive?: boolean;
  isFirst?: boolean;
  isLast?: boolean;
}

const StyledTabLight = styled(Box)<TabProps>(({ theme, isActive, isFirst }) => ({
  display: 'flex',
  alignItems: 'center',
  paddingLeft: theme.spacing(1),
  paddingRight: theme.spacing(1),
  minWidth: '10ch',
  maxWidth: '20ch',
  overflow: 'hidden',
  borderTop: `1px solid ${TreeColors.light.border}`,
  borderLeft: isFirst ? `1px solid ${TreeColors.light.border}` : 'none',
  borderRight: `1px solid ${TreeColors.light.border}`,
  borderBottom: isActive ? `1px solid ${TreeColors.light.background}` : 'none',
  backgroundColor: isActive ? TreeColors.light.background : TreeColors.light.surface,
  cursor: 'pointer',
  marginBottom: isActive ? '-1px' : 0
}));

const StyledTabDark = styled(Box)<TabProps>(({ theme, isActive, isFirst }) => ({
  display: 'flex',
  alignItems: 'center',
  paddingLeft: theme.spacing(1),
  paddingRight: theme.spacing(1),
  minWidth: '10ch',
  maxWidth: '20ch',
  overflow: 'hidden',
  borderTop: `1px solid ${TreeColors.dark.border}`,
  borderLeft: isFirst ? `1px solid ${TreeColors.dark.border}` : 'none',
  borderRight: `1px solid ${TreeColors.dark.border}`,
  borderBottom: isActive ? `1px solid ${TreeColors.dark.surface}` : 'none',
  backgroundColor: isActive ? TreeColors.dark.surface : TreeColors.dark.background,
  cursor: 'pointer',
  marginBottom: isActive ? '-1px' : 0
}));


const StyledTabContainerLight = styled(Box)(() => ({
  height: 35,
  display: 'flex',
  borderBottom: `1px solid ${TreeColors.light.border}`,
  width: '100%'
}));

const StyledTabContainerDark = styled(Box)(() => ({
  height: 35,
  display: 'flex',
  borderBottom: `1px solid ${TreeColors.dark.border}`,
  width: '100%'
}));