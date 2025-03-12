import React from 'react';
import { Button, Divider, Stack, Typography } from '@mui/material';
import CreateOutlinedIcon from '@mui/icons-material/CreateOutlined';

import ListIcon from '@mui/icons-material/ListAlt';
import BuildOutlinedIcon from '@mui/icons-material/BuildOutlined';
import TaskOutlinedIcon from '@mui/icons-material/TaskOutlined';
import SettingsOutlinedIcon from '@mui/icons-material/SettingsOutlined';
import DashboardCustomizeOutlinedIcon from '@mui/icons-material/DashboardCustomizeOutlined';
import NetworkCheckIcon from '@mui/icons-material/NetworkCheck';
import BeenhereOutlinedIcon from '@mui/icons-material/BeenhereOutlined';
import EditNoteOutlinedIcon from '@mui/icons-material/EditNoteOutlined';
import ThumbUpAltOutlinedIcon from '@mui/icons-material/ThumbUpAltOutlined';
import CloudQueueIcon from '@mui/icons-material/CloudQueue';


import { useIntl } from 'react-intl';
import { useNavigate } from '@tanstack/react-router'

import {  useUtilityClasses } from '../burger/eveli-shell/useUtilityClasses';

import logo from '../uiDev/logoLifeDigitalDark.svg';
import { ComposeSelect } from '../uiDev/ComposeSelect';
import * as Burger from '@/burger';

type NavType = 'TASKS' | 'DASHBOARD' | 'PROCESSES' | 'FORMS' | 'WRENCH' | 'STENCIL' | 'WORKFLOWS' | 'FEEDBACK' | 'QUEUES' | 'PUBLICATIONS';



export const Secondary: React.FC = () => {
  const intl = useIntl();
  const navigate = useNavigate();
  const classes = useUtilityClasses();

  const [anchorEl, setAnchorEl] = React.useState<HTMLButtonElement | null>(null);
  const [activeButton, setActiveButton] = React.useState<NavType>('TASKS')

  const handleComposeSelectClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleComposeSelectClose = () => {
    setAnchorEl(null);
  };


  return (<>
    <ComposeSelect open={!!anchorEl} anchorEl={anchorEl} onClose={handleComposeSelectClose} />

    <Burger.EveliShellExplorer>
      <div className={classes.logoContainer}>
        <img src={logo} className={classes.logo} />
      </div>

      <Button startIcon={<CreateOutlinedIcon />} className={classes.composeButton} onClick={handleComposeSelectClick}>Compose</Button>

      <Button startIcon={<TaskOutlinedIcon />}
        variant={activeButton === 'TASKS' ? 'explorerActive' : 'explorerInactive'} 
        onClick={() => navigate({
          from: '/secured/$locale',
          to: '/secured/$locale/worker/tasks'
        })}
      >
        {intl.formatMessage({ id: 'menu.tasks' })}
      </Button>

      <Button startIcon={<DashboardCustomizeOutlinedIcon />}
        variant={activeButton === 'DASHBOARD' ?  'explorerActive' : 'explorerInactive'}
        onClick={() => navigate({
          from: '/secured/$locale',
          to: '/secured/$locale/worker/dashboard'
        })}
      >
        {intl.formatMessage({ id: 'menu.dashboard' })}
      </Button>

      <Button startIcon={<NetworkCheckIcon />}
        variant={activeButton === 'PROCESSES' ?  'explorerActive' : 'explorerInactive'}
        onClick={() => navigate({
          from: '/secured/$locale',
          to: '/secured/$locale/worker/monitoring'
        })}
      >
        {intl.formatMessage({ id: 'menu.processes' })}
      </Button>

      <Button startIcon={<ListIcon />}
      variant={activeButton === 'FORMS' ?  'explorerActive' : 'explorerInactive'}
        onClick={() => navigate({
          from: '/secured/$locale',
          to: '/secured/$locale/assets/forms'
        })}>
        {intl.formatMessage({ id: 'menu.forms' })}
      </Button>

      <Button startIcon={<BuildOutlinedIcon />}
        variant={activeButton === 'WRENCH' ?  'explorerActive' : 'explorerInactive'}
        onClick={() => navigate({
          from: '/secured/$locale',
          to: '/secured/$locale/assets/wrench',
          search: { explorer: [] }
        })}
        
      >
        {intl.formatMessage({ id: 'menu.flow' })}
      </Button>

      <Button startIcon={<EditNoteOutlinedIcon />}
        variant={activeButton === 'STENCIL' ?  'explorerActive' : 'explorerInactive'}
        onClick={() => navigate({
          from: '/secured/$locale',
          to: '/secured/$locale/assets/stencil',
          search: { explorer: [] }
        })}>
        {intl.formatMessage({ id: 'menu.content' })}
      </Button>

      <Button startIcon={<SettingsOutlinedIcon />}
        variant={activeButton === 'WORKFLOWS' ?  'explorerActive' : 'explorerInactive'}
        onClick={() => navigate({
          from: '/secured/$locale',
          to: '/secured/$locale/assets/services'
        })}>
        {intl.formatMessage({ id: 'menu.workflows' })}
      </Button>

      <Button startIcon={<ThumbUpAltOutlinedIcon />}
        variant={activeButton === 'FEEDBACK' ?  'explorerActive' : 'explorerInactive'}
        onClick={() => navigate({
          from: '/secured/$locale',
          to: '/secured/$locale/worker/feedback'
        })}
      >
        {intl.formatMessage({ id: 'menu.feedback' })}
      </Button>

      <Button startIcon={<CloudQueueIcon />}
        variant={activeButton === 'QUEUES' ?  'explorerActive' : 'explorerInactive'}
        onClick={() => navigate({
          from: '/secured/$locale',
          to: '/secured/$locale/worker/queues'
        })}
      >
        {intl.formatMessage({ id: 'menu.queues' })}
      </Button>

      <Button startIcon={<BeenhereOutlinedIcon />}
        variant={activeButton === 'PUBLICATIONS' ?  'explorerActive' : 'explorerInactive'}
        onClick={() => navigate({
          from: '/secured/$locale',
          to: '/secured/$locale/worker/publications'
        })}
      >
        {intl.formatMessage({ id: 'menu.publications' })}
      </Button>



    </Burger.EveliShellExplorer>
  </>
  )
}

