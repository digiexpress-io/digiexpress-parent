import React from 'react';
import { Box } from '@mui/material';
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

import { BrowserRouter, useNavigate } from 'react-router-dom';

import { useConfig } from './context/ConfigContext';
import { useUserInfo } from './context/UserContext';

import { MenuItem, MenuItemProps } from './explorer';


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



export const Secondary: React.FC = () => {

  const ENV_TYPE = process.env.VITE_ENV_TYPE || 'test';
  /* TODO: need admin role here*/
  const isTestEnv = ENV_TYPE !== 'prod';
  const config = useConfig();
  const userInfo = useUserInfo();


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
    <BrowserRouter>
      <Box sx={{ backgroundColor: "explorer.main", height: '100%' }}>
        <Box display="flex" >
          <Explorer />
        </Box>
      </Box>
    </BrowserRouter>
  )
}


/*

<List sx={{ backgroundColor: theme.palette.explorer.main, height: '100%' }}>
            <ListItem disableGutters disablePadding>
              <Tooltip title={intl.formatMessage({ id: 'menu.tasks' })} disableHoverListener={open} >
                <ListItemLink to='/ui/tasks' icon={<ListIcon />} >{intl.formatMessage({ id: 'menu.tasks' })}</ListItemLink>
              </Tooltip>
            </ListItem>

            {showDashboard &&
              <ListItem disableGutters disablePadding>
                <Tooltip title={intl.formatMessage({ id: 'menu.dashboard' })} disableHoverListener={open} >
                  <ListItemLink to='/ui/dashboard' icon={<DashboardIcon />}>{intl.formatMessage({ id: 'menu.dashboard' })}</ListItemLink>
                </Tooltip>
              </ListItem>
            }

            {isTestEnv &&
              <ListItem disableGutters disablePadding>
                <Tooltip title={intl.formatMessage({ id: 'menu.processes' })} disableHoverListener={open} >
                  <ListItemLink to='/ui/processes' icon={<NetworkCheckIcon />}>{intl.formatMessage({ id: 'menu.processes' })} </ListItemLink>
                </Tooltip>
              </ListItem>
            }

            {ENV_TYPE !== 'prod' &&
              <React.Fragment>
                <ListItem disableGutters disablePadding>
                  <Tooltip title={intl.formatMessage({ id: 'menu.tools' })} disableHoverListener={open} >
                    <ListItemButton onClick={handleToolsClick}>
                      <ListItemIcon sx={{ color: explorerItemColor }}><BusinessCenterIcon /></ListItemIcon>
                      <Typography sx={{ color: explorerItemColor }}>{intl.formatMessage({ id: 'menu.tools' })}</Typography>
                      <div style={{ flexGrow: 1 }} />
                      {isToolsOpen ? <ExpandLess sx={{ color: explorerItemColor }} /> : <ExpandMore sx={{ color: explorerItemColor }} />}
                    </ListItemButton>
                  </Tooltip>
                </ListItem>

                <Collapse in={isToolsOpen} timeout="auto" unmountOnExit>
                  <List component="div" disablePadding sx={nestedStyle}>
                    <ListItem disableGutters disablePadding>
                      <Tooltip title={intl.formatMessage({ id: 'menu.forms' })} disableHoverListener={open} >
                        <ListItemLink to='/ui/forms' icon={<ViewListIcon />}>{intl.formatMessage({ id: 'menu.forms' })}</ListItemLink>
                      </Tooltip>
                    </ListItem>

                    <ListItem disableGutters disablePadding>
                      <Tooltip title={intl.formatMessage({ id: 'menu.flow' })} disableHoverListener={open} >
                        <ExternalLink showEndIcon={open} to={'/wrench/ide'} icon={<BuildIcon />}>{intl.formatMessage({ id: 'menu.flow' })}</ExternalLink>
                      </Tooltip>
                    </ListItem>
                    <ListItem disableGutters disablePadding>
                      <Tooltip title={intl.formatMessage({ id: 'menu.content' })} disableHoverListener={open}>
                        <ExternalLink showEndIcon={open} to={'/stencil/ide'} icon={<SubjectIcon />}>{intl.formatMessage({ id: 'menu.content' })}</ExternalLink>
                      </Tooltip>
                    </ListItem>

                    {config.calendarUrl &&
                      <ListItem disableGutters disablePadding>
                        <Tooltip title={intl.formatMessage({ id: 'menu.calendar' })} disableHoverListener={open}>
                          <ExternalLink showEndIcon={open} to={config.calendarUrl} icon={<CalendarMonthIcon />}>{intl.formatMessage({ id: 'menu.calendar' })}</ExternalLink>
                        </Tooltip>
                      </ListItem>
                    }
                  </List>
                </Collapse>

                <Tooltip title={intl.formatMessage({ id: 'menu.services' })} disableHoverListener={open} >
                  <ListItemButton onClick={handleServiceClick}>
                    <ListItemIcon sx={{ color: explorerItemColor }}><VisibilityIcon /></ListItemIcon>
                    <Typography sx={{ color: explorerItemColor }}>{intl.formatMessage({ id: 'menu.services' })}</Typography>
                    <div style={{ flexGrow: 1 }} />
                    {isServicesOpen ? <ExpandLess sx={{ color: explorerItemColor }} /> : <ExpandMore sx={{ color: explorerItemColor }} />}
                  </ListItemButton>
                </Tooltip>

                <Collapse in={isServicesOpen} timeout="auto" unmountOnExit>
                  <ListItem disableGutters disablePadding>
                    <Tooltip title={intl.formatMessage({ id: 'menu.workflows' })} disableHoverListener={open} >
                      <List component="div" disablePadding sx={nestedStyle}>
                        <ListItemLink to='/ui/workflows' icon={<SettingsIcon />}>{intl.formatMessage({ id: 'menu.workflows' })}</ListItemLink>
                      </List>
                    </Tooltip>
                  </ListItem>

                  <ListItem disableGutters disablePadding>
                    <Tooltip title={intl.formatMessage({ id: 'menu.workflowReleases' })} disableHoverListener={open} >
                      <List component="div" disablePadding sx={nestedStyle}>
                        <ListItemLink to='/ui/workflowReleases' icon={<InventoryIcon />}>{intl.formatMessage({ id: 'menu.workflowReleases' })}</ListItemLink>
                      </List>
                    </Tooltip>
                  </ListItem>

                  <ListItem disableGutters disablePadding>
                    <Tooltip title={intl.formatMessage({ id: 'menu.releases' })} disableHoverListener={open} >
                      <List component="div" disablePadding sx={nestedStyle}>
                        <ListItemLink to='/ui/releases' icon={<AllInbox />}>{intl.formatMessage({ id: 'menu.releases' })}</ListItemLink>
                      </List>
                    </Tooltip>
                  </ListItem>
                </Collapse>
              </React.Fragment>
            }




            {isTestEnv &&
              <Tooltip title={intl.formatMessage({ id: 'menu.help' })} disableHoverListener={open} >
                <ListItemLink to='/ui/help' icon={<HelpIcon />}>{intl.formatMessage({ id: 'menu.help' })} </ListItemLink>
              </Tooltip>
            }
          </List>

*/