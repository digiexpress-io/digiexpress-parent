import React from 'react';
import { alpha, Box, styled, SxProps, Typography } from '@mui/material';
import { SimpleTreeView } from '@mui/x-tree-view';


import ListIcon from '@mui/icons-material/ListAlt';
import BuildIcon from '@mui/icons-material/Build';
import ChecklistIcon from '@mui/icons-material/Checklist';
import SettingsIcon from '@mui/icons-material/Settings';
import DashboardIcon from '@mui/icons-material/Dashboard';
import NetworkCheckIcon from '@mui/icons-material/NetworkCheck';
import BeenhereIcon from '@mui/icons-material/Beenhere';
import MenuBookIcon from '@mui/icons-material/MenuBook';
import ThumbUpAltIcon from '@mui/icons-material/ThumbUpAlt';
import CloudQueueIcon from '@mui/icons-material/CloudQueue';

import { useNavigate } from 'react-router-dom';

import { MenuItem, MenuItemProps } from './explorer';
import { useIntl } from 'react-intl';


const iconSize: SxProps = {
  fontSize: '13pt'
}

const menuItems: MenuItemProps[] = [
  { id: 'menu.tasks', to: '/ui/tasks', icon: <ChecklistIcon sx={iconSize} /> },
  { id: 'menu.dashboard', to: '/ui/dashboard', icon: <DashboardIcon sx={iconSize} /> },
  { id: 'menu.processes', to: '/ui/processes', icon: <NetworkCheckIcon sx={iconSize} /> },
  { id: 'menu.forms', to: '/ui/forms', icon: <ListIcon sx={iconSize} /> },
  { id: 'menu.flow', to: '/wrench/ide', icon: <BuildIcon sx={iconSize} /> },
  { id: 'menu.content', to: '/ui/content', icon: <MenuBookIcon sx={iconSize} /> },
  { id: 'menu.workflows', to: '/ui/workflows', icon: <SettingsIcon sx={iconSize} /> },
  { id: 'menu.feedback', to: '/feedback', icon: <ThumbUpAltIcon sx={iconSize} /> },
  { id: 'menu.queues', to: '/queues', icon: <CloudQueueIcon sx={iconSize} /> },
  { id: 'menu.publications', to: '/ui/publications', icon: <BeenhereIcon sx={iconSize} /> },
]

export const Explorer: React.FC<{}> = () => {
  const navigate = useNavigate();

  const handleMenuItemClick = (to?: string) => {
    if (to) {
      navigate(to);
    } 
  };

  return (<>

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
  </>
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

  const intl = useIntl();

  return (<>
      <ExplorerTitleBar>
        <Typography sx={{ color: 'white', fontStyle: 'italic', fontFamily: 'serif' }}>My Logo</Typography>
        <Typography>{intl.formatMessage({ id: 'explorer.title' })}</Typography>
      </ExplorerTitleBar>
      <Box display="flex" flexDirection='column' flexGrow={1}>
        <Explorer />
      </Box>
  </>
  )
}

