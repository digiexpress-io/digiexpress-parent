import { List, Collapse, ListItemButton, useTheme, ListItem, Typography } from '@mui/material';
import ListIcon from '@mui/icons-material/ListAlt';
import BuildIcon from '@mui/icons-material/Build';
import ViewListIcon from '@mui/icons-material/ViewList';
import SettingsIcon from '@mui/icons-material/Settings';
import HelpIcon from '@mui/icons-material/Help';
import DashboardIcon from '@mui/icons-material/Dashboard';
import ExpandLess from '@mui/icons-material/ExpandLess';
import ExpandMore from '@mui/icons-material/ExpandMore';
import BusinessCenterIcon from '@mui/icons-material/BusinessCenter';
import VisibilityIcon from '@mui/icons-material/Visibility';
import NetworkCheckIcon from '@mui/icons-material/NetworkCheck';
import SubjectIcon from '@mui/icons-material/Subject';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import InventoryIcon from '@mui/icons-material/Inventory';
import Tooltip from '@mui/material/Tooltip';
import React, { useState } from 'react';
import { ListItemLink } from './ListItemLink';
import { ExternalLink } from './ExternalLink';
import { useIntl } from 'react-intl';
import { ListItemIcon } from '@mui/material';
import { useConfig } from '../context/ConfigContext';
import { useUserInfo } from '../context/UserContext';
import { AllInbox } from '@mui/icons-material';



interface AppMenuProps {
  open: boolean
}

export const AppMenu: React.FC<AppMenuProps> = ({ open }) => {
  const ENV_TYPE = process.env.VITE_ENV_TYPE || 'test';
  /* TODO: need admin role here*/
  const isTestEnv = ENV_TYPE !== 'prod';
  const intl = useIntl();
  const config = useConfig();
  const theme = useTheme();
  const userInfo = useUserInfo();
  const [isServicesOpen, setServicesOpen] = useState(false);
  const [isToolsOpen, setToolsOpen] = useState(false);

  const nestedStyle = open ? { pl: theme.spacing(1), pb: theme.spacing(1) } : {};

  const explorerItemColor = theme.palette.explorerItem.main;


  const handleServiceClick = () => {
    setServicesOpen(!isServicesOpen);
  }
  const handleToolsClick = () => {
    setToolsOpen(!isToolsOpen);
  }


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
                  <ExternalLink showEndIcon={open} to={config.wrenchIdeUrl || '/wrench/ide'} icon={<BuildIcon />}>{intl.formatMessage({ id: 'menu.flow' })}</ExternalLink>
                </Tooltip>
              </ListItem>

              {config.contentRepositoryUrl &&
                <ListItem disableGutters disablePadding>
                  <Tooltip title={intl.formatMessage({ id: 'menu.content' })} disableHoverListener={open}>
                    <ExternalLink showEndIcon={open} to={config.contentRepositoryUrl} icon={<SubjectIcon />}>{intl.formatMessage({ id: 'menu.content' })}</ExternalLink>
                  </Tooltip>
                </ListItem>
              }
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
  );
}
