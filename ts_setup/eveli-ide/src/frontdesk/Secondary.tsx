import React from 'react';
import { alpha, Box, styled, Typography } from '@mui/material';
import { SimpleTreeView } from '@mui/x-tree-view';


import ListIcon from '@mui/icons-material/ListAlt';
import BuildIcon from '@mui/icons-material/Build';
import ViewListIcon from '@mui/icons-material/ViewList';
import SettingsIcon from '@mui/icons-material/Settings';
import HelpIcon from '@mui/icons-material/Help';
import DashboardIcon from '@mui/icons-material/Dashboard';
import BusinessCenterIcon from '@mui/icons-material/BusinessCenter';
import NetworkCheckIcon from '@mui/icons-material/NetworkCheck';
import AllInboxIcon from '@mui/icons-material/AllInbox';
import SubjectIcon from '@mui/icons-material/Subject';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import TaskAltIcon from '@mui/icons-material/TaskAlt';

import { BrowserRouter, useNavigate } from 'react-router-dom';

import { useConfig } from './context/ConfigContext';
import { useUserInfo } from './context/UserContext';

import { MenuItem, MenuItemProps } from './explorer';
import { useIntl } from 'react-intl';
import { fontStyle, textTransform } from '@mui/system';


const menuItems: MenuItemProps[] = [
  { nodeId: '1', to: '/ui/tasks', labelText: 'menu.tasks', icon: <ViewListIcon fontSize='small' />, onClick: () => { } },
  { nodeId: '2', to: '/ui/dashboard', labelText: 'menu.dashboard', icon: <DashboardIcon fontSize='small' />, onClick: () => { } },
  { nodeId: '3', to: '/ui/processes', labelText: 'menu.processes', icon: <NetworkCheckIcon fontSize='small' />, onClick: () => { } },
  { nodeId: '5', to: '/ui/forms', labelText: 'menu.forms', icon: <ListIcon fontSize='small' />, onClick: () => { } },
  { nodeId: '6', to: '/wrench/ide', labelText: 'menu.flow', icon: <BuildIcon fontSize='small' />, onClick: () => { } },
  { nodeId: '7', to: '/ui/content', labelText: 'menu.content', icon: <SubjectIcon fontSize='small' />, onClick: () => { } },
  { nodeId: '8', to: '/ui/calendar', labelText: 'menu.calendar', icon: <CalendarMonthIcon fontSize='small' />, onClick: () => { } },
  { nodeId: '10', to: '/ui/workflows', labelText: 'menu.workflows', icon: <SettingsIcon fontSize='small' />, onClick: () => { } },
  { nodeId: '11', to: '/ui/workflowReleases', labelText: 'menu.workflowReleases', icon: <AllInboxIcon fontSize='small' />, onClick: () => { } },
  { nodeId: '12', to: '/ui/releases', labelText: 'menu.releases', icon: <BusinessCenterIcon fontSize='small' />, onClick: () => { } },
  { nodeId: '13', to: '/ui/help', labelText: 'menu.help', icon: <HelpIcon fontSize='small' />, onClick: () => { } },
]


export const Explorer: React.FC<{}> = () => {
  const navigate = useNavigate();

  const handleMenuItemClick = (to?: string) => {
    if (to) {
      navigate(to);
    }
  };
  return (
    <SimpleTreeView>
      {menuItems.map((item) => (
        <MenuItem
          key={item.nodeId}
          icon={item.icon}
          labelText={item.labelText}
          nodeId={item.nodeId}
          onClick={() => handleMenuItemClick(item.to)}
        />
      )
      )}
    </SimpleTreeView>
  );
}

const ExplorerTitleBar = styled(Box)(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  minWidth: "unset",
  paddingTop: theme.spacing(2),
  paddingBottom: theme.spacing(2),
  paddingLeft: theme.spacing(2),
  color: theme.palette.explorerItem.dark,
  backgroundColor: alpha(theme.palette.explorerItem.dark, .2),
  '& .MuiTypography-root': {
    marginLeft: theme.spacing(3),
    fontSize: theme.typography.caption.fontSize,
    textTransform: 'uppercase',
  }
}));



export const Secondary: React.FC = () => {

  const ENV_TYPE = process.env.VITE_ENV_TYPE || 'test';
  /* TODO: need admin role here*/
  const isTestEnv = ENV_TYPE !== 'prod';
  const config = useConfig();
  const userInfo = useUserInfo();
  const intl = useIntl();

  const isTaskAdmin = () => {
    if (config.taskAdminGroups?.length) {
      if (userInfo.hasRole(...config.taskAdminGroups)) {
        return true;
      }
      return false;
    }
    return true;
  }
  const showDashboard = isTestEnv || isTaskAdmin();

  return (
    <Box sx={{ backgroundColor: "explorer.main", height: '100%' }}>
      <ExplorerTitleBar>
        <TaskAltIcon />
        <Typography>{intl.formatMessage({ id: 'explorer.title' })}</Typography>
      </ExplorerTitleBar>
      <Box display="flex" >
        <Explorer />
      </Box>
    </Box>
  )
}
