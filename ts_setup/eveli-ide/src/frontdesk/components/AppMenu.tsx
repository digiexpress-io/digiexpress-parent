import { Divider, List, Collapse, ListItem, ListItemText, ListItemButton } from '@mui/material';
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
  const userInfo = useUserInfo();
  const [isServicesOpen, setServicesOpen] = useState(false);
  const [isToolsOpen, setToolsOpen] = useState(false);

  const nestedStyle = open ? { pl: 4 } : {};

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
    <List sx={{ pt: 2 }}>
      <Tooltip title={intl.formatMessage({ id: 'menu.tasks' })} disableHoverListener={open} >
        <div>
          <ListItemLink button primary={intl.formatMessage({ id: 'menu.tasks' })} to='/ui/tasks' icon={<ListIcon />} />
        </div>
      </Tooltip>

      {showDashboard &&
        <Tooltip title={intl.formatMessage({ id: 'menu.dashboard' })} disableHoverListener={open} >
          <div>
            <ListItemLink button primary={intl.formatMessage({ id: 'menu.dashboard' })} to='/ui/dashboard' icon={<DashboardIcon />} />
          </div>
        </Tooltip>
      }

      {isTestEnv &&
        <Tooltip title={intl.formatMessage({ id: 'menu.processes' })} disableHoverListener={open} >
          <div>
            <ListItemLink button primary={intl.formatMessage({ id: 'menu.processes' })} to='/ui/processes' icon={<NetworkCheckIcon />} />
          </div>
        </Tooltip>
      }
      <Divider />
      {ENV_TYPE !== 'prod' &&
        <React.Fragment>
          <Tooltip title={intl.formatMessage({ id: 'menu.tools' })} disableHoverListener={open} >
            <ListItemButton onClick={handleToolsClick}>
              <ListItemIcon>
                <BusinessCenterIcon />
              </ListItemIcon>
              <ListItemText primary={intl.formatMessage({ id: 'menu.tools' })} />
              {isToolsOpen ? <ExpandLess /> : <ExpandMore />}
            </ListItemButton>
          </Tooltip>
          <Collapse in={isToolsOpen} timeout="auto" unmountOnExit>
            <List component="div" disablePadding sx={nestedStyle}>
              <Tooltip title={intl.formatMessage({ id: 'menu.forms' })} disableHoverListener={open} >
                <div>
                  <ListItemLink button primary={intl.formatMessage({ id: 'menu.forms' })}
                    to='/ui/forms' icon={<ViewListIcon />} />
                </div>
              </Tooltip>
              <Tooltip title={intl.formatMessage({ id: 'menu.flow' })} disableHoverListener={open} >
                <div>
                  <ExternalLink showEndIcon={open} button primary={intl.formatMessage({ id: 'menu.flow' })}
                    to={config.wrenchIdeUrl || '/wrench/ide'} icon={<BuildIcon />} />
                </div>
              </Tooltip>

              {config.contentRepositoryUrl &&
                <Tooltip title={intl.formatMessage({ id: 'menu.content' })} disableHoverListener={open} >
                  <div>
                    <ExternalLink showEndIcon={open} button primary={intl.formatMessage({ id: 'menu.content' })}
                      to={config.contentRepositoryUrl} icon={<SubjectIcon />} />
                  </div>
                </Tooltip>
              }
              {config.calendarUrl &&
                <Tooltip title={intl.formatMessage({ id: 'menu.calendar' })} disableHoverListener={open} >
                  <div>
                    <ExternalLink showEndIcon={open} button primary={intl.formatMessage({ id: 'menu.calendar' })}
                      to={config.calendarUrl} icon={<CalendarMonthIcon />} />
                  </div>
                </Tooltip>
              }
              <Divider />
            </List>
          </Collapse>
          <Tooltip title={intl.formatMessage({ id: 'menu.services' })} disableHoverListener={open} >
            <ListItemButton onClick={handleServiceClick}>
              <ListItemIcon>
                <VisibilityIcon />
              </ListItemIcon>
              <ListItemText primary={intl.formatMessage({ id: 'menu.services' })} />
              {isServicesOpen ? <ExpandLess /> : <ExpandMore />}
            </ListItemButton>
          </Tooltip>

          <Collapse in={isServicesOpen} timeout="auto" unmountOnExit>
            <Tooltip title={intl.formatMessage({ id: 'menu.workflows' })} disableHoverListener={open} >
              <List component="div" disablePadding sx={nestedStyle}>
                <ListItemLink button primary={intl.formatMessage({ id: 'menu.workflows' })}
                  to='/ui/workflows' icon={<SettingsIcon />} />
              </List>
            </Tooltip>
            <Tooltip title={intl.formatMessage({ id: 'menu.workflowReleases' })} disableHoverListener={open} >
              <List component="div" disablePadding sx={nestedStyle}>
                <ListItemLink button primary={intl.formatMessage({ id: 'menu.workflowReleases' })}
                  to='/ui/workflowReleases' icon={<InventoryIcon />} />
              </List>
            </Tooltip>
            <Tooltip title={intl.formatMessage({ id: 'menu.releases' })} disableHoverListener={open} >
              <List component="div" disablePadding sx={nestedStyle}>
                <ListItemLink button primary={intl.formatMessage({ id: 'menu.releases' })} to='/ui/releases' icon={<AllInbox />} />
              </List>
            </Tooltip>
            <Divider />
          </Collapse>
          <Divider />
        </React.Fragment>
      }
      {isTestEnv &&
        <Tooltip title={intl.formatMessage({ id: 'menu.help' })} disableHoverListener={open} >
          <div>
            <ListItemLink button primary={intl.formatMessage({ id: 'menu.help' })} to='/ui/help' icon={<HelpIcon />} />
          </div>
        </Tooltip>
      }
    </List>
  );
}
