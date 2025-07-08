import React from 'react';
import { Button } from '@mui/material';
import CreateOutlinedIcon from '@mui/icons-material/CreateOutlined';

import TaskOutlinedIcon from '@mui/icons-material/TaskOutlined';
import DashboardCustomizeOutlinedIcon from '@mui/icons-material/DashboardCustomizeOutlined';
import NetworkCheckIcon from '@mui/icons-material/NetworkCheck';
import ThumbUpAltOutlinedIcon from '@mui/icons-material/ThumbUpAltOutlined';
import CloudQueueIcon from '@mui/icons-material/CloudQueue';
import TableChartOutlinedIcon from '@mui/icons-material/TableChartOutlined';
import PersonOutlineOutlinedIcon from '@mui/icons-material/PersonOutlineOutlined';
import WorkOutlineOutlinedIcon from '@mui/icons-material/WorkOutlineOutlined';

import { useIntl } from 'react-intl';
import { useLocation, useNavigate } from '@tanstack/react-router'

import { useUtilityClasses } from '../eveli-shell/useUtilityClasses';
import { EveliShellExplorer } from '@/eveli-shell-explorer';
import { EveliPermissions } from '@/eveli-permissions';
import { EveliTenantFeatureEnabled } from '@/api-tenant-config';



export const Secondary: React.FC = () => {
  const intl = useIntl();
  const navigate = useNavigate();
  const classes = useUtilityClasses();
  const location = useLocation()


  return (<>

    <EveliShellExplorer>
      <EveliPermissions id='CREATE_TASK'>
        <Button startIcon={<CreateOutlinedIcon />} className={classes.composeButton}
          onClick={() => {
            navigate({
              from: '/secured/$locale',
              to: '/secured/$locale/worker/tasks/create'
            })
          }}>
          {intl.formatMessage({ id: 'button.compose' })}
        </Button>
      </EveliPermissions>

      <EveliPermissions id='NAV_TO_TASKS'>
        <Button startIcon={<TaskOutlinedIcon />}
          variant={location.pathname.includes('tasks') ? 'explorerActive' : 'explorerInactive'}
          onClick={() => navigate({
            from: '/secured/$locale',
            to: '/secured/$locale/worker/tasks'
          })}
        >
          {intl.formatMessage({ id: 'menu.tasks' })}
        </Button>
      </EveliPermissions>

      <EveliPermissions id='NAV_TO_TASKS_DASHBOARD'>
        <Button startIcon={<DashboardCustomizeOutlinedIcon />}
          variant={location.pathname.endsWith('dashboard') ? 'explorerActive' : 'explorerInactive'}
          onClick={() => navigate({
            from: '/secured/$locale',
            to: '/secured/$locale/worker/dashboard'
          })}
        >
          {intl.formatMessage({ id: 'menu.dashboard' })}
        </Button>
      </EveliPermissions>

      <EveliPermissions id='NAV_TO_TASKS_MONITORING'>
        <Button startIcon={<NetworkCheckIcon />}
          variant={location.pathname.endsWith('monitoring') ? 'explorerActive' : 'explorerInactive'}
          onClick={() => navigate({
            from: '/secured/$locale',
            to: '/secured/$locale/worker/monitoring'
          })}
        >
          {intl.formatMessage({ id: 'menu.processes' })}
        </Button>
      </EveliPermissions>

      <EveliTenantFeatureEnabled id='FEEDBACK_ENABLED'>
        <EveliPermissions id='NAV_TO_TASKS_FEEDBACK'>
          <Button startIcon={<ThumbUpAltOutlinedIcon />}
            variant={location.pathname.endsWith('feedback') ? 'explorerActive' : 'explorerInactive'}
            onClick={() => navigate({
              from: '/secured/$locale',
              to: '/secured/$locale/worker/feedback'
            })}
          >
            {intl.formatMessage({ id: 'menu.feedback' })}
          </Button>
        </EveliPermissions>
      </EveliTenantFeatureEnabled>


      <EveliTenantFeatureEnabled id='QUEUES_ENABLED'>
        <EveliPermissions id='NAV_TO_TASKS_QUEUES'>
          <Button startIcon={<CloudQueueIcon />}
            variant={location.pathname.endsWith('queues') ? 'explorerActive' : 'explorerInactive'}
            onClick={() => navigate({
              from: '/secured/$locale',
              to: '/secured/$locale/worker/queues'
            })}
          >
            {intl.formatMessage({ id: 'menu.queues' })}
          </Button>
        </EveliPermissions>
      </EveliTenantFeatureEnabled>

      <EveliTenantFeatureEnabled id='PROFILE_ENABLED'>
        <Button startIcon={<PersonOutlineOutlinedIcon />}
          variant={location.pathname.endsWith('profile') ? 'explorerActive' : 'explorerInactive'}
          onClick={() => navigate({
            from: '/secured/$locale',
            to: '/secured/$locale/worker/profile'
          })}
        >
          {intl.formatMessage({ id: 'menu.profile' })}
        </Button>
      </EveliTenantFeatureEnabled>

      <EveliTenantFeatureEnabled id='BATCHES'>
        <Button startIcon={<WorkOutlineOutlinedIcon />}
          variant={location.pathname.endsWith('batches') ? 'explorerActive' : 'explorerInactive'}
          onClick={() => navigate({
            from: '/secured/$locale',
            to: '/secured/$locale/worker/batches'
          })}
        >
          {intl.formatMessage({ id: 'menu.batches', defaultMessage: 'Batches' })}
        </Button>
      </EveliTenantFeatureEnabled>

    </EveliShellExplorer>
  </>
  )
}

