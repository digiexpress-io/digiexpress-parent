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
import FormatListNumberedIcon from '@mui/icons-material/FormatListNumbered';
import EmailIcon from '@mui/icons-material/Email';
import DeliveryDiningIcon from '@mui/icons-material/DeliveryDining';


import { useNavigate } from '@tanstack/react-router';

import { MenuItem } from './explorer';
import { useIntl } from 'react-intl';


const iconSize: SxProps = {
  fontSize: '13pt'
}


export const Explorer: React.FC<{}> = () => {
  const navigate = useNavigate();


  return (
    <SimpleTreeView>
      <MenuItem
        icon={<ChecklistIcon sx={iconSize} />}
        id='menu.tasks'
        onClick={() => navigate({
          from: '/secured/$locale/worker',
          to: '/secured/$locale/worker/tasks'
        })}
      />
      <MenuItem
        icon={<DashboardIcon sx={iconSize} />}
        id='menu.dashboard'
        onClick={() => navigate({
          from: '/secured/$locale/worker',
          to: '/secured/$locale/worker/dashboard'
        })}
      />
      <MenuItem
        icon={<NetworkCheckIcon sx={iconSize} />}
        id='menu.processes'
        onClick={() => navigate({
          from: '/secured/$locale/worker',
          to: '/secured/$locale/worker/monitoring'
        })}
      />

      <MenuItem
        icon={<ListIcon sx={iconSize} />}
        id='menu.forms'
        onClick={() => navigate({
          from: '/secured/$locale/worker',
          to: '/secured/$locale/assets/forms'
        })}
      />

      <MenuItem
        icon={<BuildIcon sx={iconSize} />}
        id='menu.flow'
        onClick={() => navigate({
          from: '/secured/$locale/worker',
          to: '/secured/$locale/assets/wrench'
        })}
      />

      <MenuItem
        icon={<MenuBookIcon sx={iconSize} />}
        id='menu.content'
        onClick={() => navigate({
          from: '/secured/$locale/worker',
          to: '/secured/$locale/assets/stencil'
        })}
      />

      <MenuItem
        icon={<SettingsIcon sx={iconSize} />}
        id='menu.workflows'
        onClick={() => navigate({
          from: '/secured/$locale/worker',
          to: '/secured/$locale/assets/services'
        })}
      />

      <MenuItem
        icon={<ThumbUpAltIcon sx={iconSize} />}
        id='menu.feedback'
        onClick={() => navigate({
          from: '/secured/$locale/worker',
          to: '/secured/$locale/worker/feedback'
        })}
      />

      <MenuItem
        icon={<CloudQueueIcon sx={iconSize} />}
        id='menu.queues'
        onClick={() => navigate({
          from: '/secured/$locale/worker',
          to: '/secured/$locale/worker/queues'
        })}
      />
      <MenuItem
        icon={<EmailIcon sx={iconSize} />}
        id='menu.messages'
        onClick={() => navigate({
          from: '/secured/$locale/worker',
          to: '/secured/$locale/worker/queues/messages'
        })}
      />
      <MenuItem
        icon={<DeliveryDiningIcon sx={iconSize} />}
        id='menu.deliveries'
        onClick={() => navigate({
          from: '/secured/$locale/worker',
          to: '/secured/$locale/worker/queues/deliveries'
        })}
      />

      <MenuItem
        icon={<BeenhereIcon sx={iconSize} />}
        id='menu.publications'
        onClick={() => navigate({
          from: '/secured/$locale/worker',
          to: '/secured/$locale/worker/publications'
        })}
      />
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
  color: theme.palette.secondary.contrastText,
  backgroundColor: alpha(theme.palette.secondary.contrastText, .2),
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

