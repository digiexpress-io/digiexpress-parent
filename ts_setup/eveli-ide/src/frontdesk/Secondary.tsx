import React from 'react';
import { Button} from '@mui/material';
import CreateOutlinedIcon from '@mui/icons-material/CreateOutlined';


import TaskOutlinedIcon from '@mui/icons-material/TaskOutlined';
import DashboardCustomizeOutlinedIcon from '@mui/icons-material/DashboardCustomizeOutlined';
import NetworkCheckIcon from '@mui/icons-material/NetworkCheck';
import BeenhereOutlinedIcon from '@mui/icons-material/BeenhereOutlined';
import ThumbUpAltOutlinedIcon from '@mui/icons-material/ThumbUpAltOutlined';
import CloudQueueIcon from '@mui/icons-material/CloudQueue';

import { useIntl } from 'react-intl';
import { useLocation, useNavigate } from '@tanstack/react-router'


import { useUtilityClasses } from '../burger/eveli-shell/useUtilityClasses';

import * as Burger from '@/burger';


export const Secondary: React.FC = () => {
  const intl = useIntl();
  const navigate = useNavigate();
  const classes = useUtilityClasses();
  const location = useLocation()



  return (<>

    <Burger.EveliShellExplorer>
      <Burger.EveliLogo />

      <Button startIcon={<CreateOutlinedIcon />} className={classes.composeButton} onClick={() => {
        navigate({
          from: '/secured/$locale',
          to: 'worker/tasks/create'
        })
      }}>{intl.formatMessage({ id: 'button.compose' })}</Button>

      <Button startIcon={<TaskOutlinedIcon />}
        variant={location.pathname.endsWith('tasks')  ? 'explorerActive' : 'explorerInactive'} 
        onClick={() => navigate({
          from: '/secured/$locale',
          to: '/secured/$locale/worker/tasks'
        })}
      >
        {intl.formatMessage({ id: 'menu.tasks' })}
      </Button>

      <Button startIcon={<DashboardCustomizeOutlinedIcon />}
        variant={location.pathname.endsWith('dashboard')  ?  'explorerActive' : 'explorerInactive'}
        onClick={() => navigate({
          from: '/secured/$locale',
          to: '/secured/$locale/worker/dashboard'
        })}
      >
        {intl.formatMessage({ id: 'menu.dashboard' })}
      </Button>

      <Button startIcon={<NetworkCheckIcon />}
        variant={location.pathname.endsWith('monitoring')  ?  'explorerActive' : 'explorerInactive'}
        onClick={() => navigate({
          from: '/secured/$locale',
          to: '/secured/$locale/worker/monitoring'
        })}
      >
        {intl.formatMessage({ id: 'menu.processes' })}
      </Button>

      <Button startIcon={<ThumbUpAltOutlinedIcon />}
        variant={location.pathname.endsWith('feedback')  ?  'explorerActive' : 'explorerInactive'}
        onClick={() => navigate({
          from: '/secured/$locale',
          to: '/secured/$locale/worker/feedback'
        })}
      >
        {intl.formatMessage({ id: 'menu.feedback' })}
      </Button>

      <Button startIcon={<CloudQueueIcon />}
        variant={location.pathname.endsWith('queues')  ?  'explorerActive' : 'explorerInactive'}
        onClick={() => navigate({
          from: '/secured/$locale',
          to: '/secured/$locale/worker/queues'
        })}
      >
        {intl.formatMessage({ id: 'menu.queues' })}
      </Button>

      <Button startIcon={<BeenhereOutlinedIcon />}
        variant={location.pathname.endsWith('publications')  ?  'explorerActive' : 'explorerInactive'}
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

