import React from 'react';
import { alpha, Box, styled, SxProps, Typography } from '@mui/material';
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

import { useNavigate } from 'react-router-dom';

import { useConfig } from './context/ConfigContext';
import { useUserInfo } from './context/UserContext';

import { MenuItem, MenuItemProps } from './explorer';
import { useIntl } from 'react-intl';

const iconSize: SxProps = {
  fontSize: '12pt'
}

const menuItems: MenuItemProps[] = [
  { id: 'menu.tasks', to: '/ui/tasks', icon: <ViewListIcon sx={iconSize} />, onClick: () => { } },
  { id: 'menu.dashboard', to: '/ui/dashboard', icon: <DashboardIcon sx={iconSize} />, onClick: () => { } },
  { id: 'menu.processes', to: '/ui/processes', icon: <NetworkCheckIcon sx={iconSize} />, onClick: () => { } },
  { id: 'menu.forms', to: '/ui/forms', icon: <ListIcon sx={iconSize} />, onClick: () => { } },
  { id: 'menu.flow', to: '/wrench/ide', icon: <BuildIcon sx={iconSize} />, onClick: () => { } },
  { id: 'menu.content', to: '/ui/content', icon: <SubjectIcon sx={iconSize} />, onClick: () => { } },
  { id: 'menu.calendar', to: '/ui/calendar', icon: <CalendarMonthIcon sx={iconSize} />, onClick: () => { } },
  { id: 'menu.workflows', to: '/ui/workflows', icon: <SettingsIcon sx={iconSize} />, onClick: () => { } },
  { id: 'menu.workflowReleases', to: '/ui/workflowReleases', icon: <AllInboxIcon sx={iconSize} />, onClick: () => { } },
  { id: 'menu.releases', to: '/ui/releases', icon: <BusinessCenterIcon sx={iconSize} />, onClick: () => { } },
  { id: 'menu.help', to: '/ui/help', icon: <HelpIcon sx={iconSize} />, onClick: () => { } },
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
          key={item.id}
          icon={item.icon}
          id={item.id}
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
  paddingTop: theme.spacing(1.5),
  paddingBottom: theme.spacing(1.5),
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
        <Typography sx={{ color: 'white', fontStyle: 'italic', fontFamily: 'serif' }}>My Logo</Typography>
        <Typography>{intl.formatMessage({ id: 'explorer.title' })}</Typography>
      </ExplorerTitleBar>
      <Box display="flex" >
        <Explorer />
      </Box>
    </Box>
  )
}
