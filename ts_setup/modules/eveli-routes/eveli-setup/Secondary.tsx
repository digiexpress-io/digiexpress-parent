import React from 'react';
import { Button } from '@mui/material';
import CreateOutlinedIcon from '@mui/icons-material/CreateOutlined';

import TaskOutlinedIcon from '@mui/icons-material/TaskOutlined';
import DashboardCustomizeOutlinedIcon from '@mui/icons-material/DashboardCustomizeOutlined';
import NetworkCheckIcon from '@mui/icons-material/NetworkCheck';
import ThumbUpAltOutlinedIcon from '@mui/icons-material/ThumbUpAltOutlined';
import CloudQueueIcon from '@mui/icons-material/CloudQueue';
import SupervisedUserCircleOutlinedIcon from '@mui/icons-material/SupervisedUserCircleOutlined';
import HealthAndSafetyOutlinedIcon from '@mui/icons-material/HealthAndSafetyOutlined';

import PersonOutlineOutlinedIcon from '@mui/icons-material/PersonOutlineOutlined';
import WorkOutlineOutlinedIcon from '@mui/icons-material/WorkOutlineOutlined';

import { useIntl } from 'react-intl';
import { useLocation, useNavigate } from '@tanstack/react-router'

import { _eveli_shell_useUtilityClasses as useUtilityClasses, EveliShellExplorer, EveliPermissions } from '@dxs-ts/eveli-primitives';
import { EveliTenantFeatureEnabled, useTenantConfigFeatures } from '@dxs-ts/eveli-api';
import { TaskCreate } from '@dxs-ts/task-composer-v2'
import { AnyTaskRoute } from '../eveli-any-task-route';



const CreateTaskButton: React.FC = () => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const navigate = useNavigate();
  const tenant = useTenantConfigFeatures();
  const [open, setOpen] = React.useState(false);

  function handleCreateTask() {

    if(tenant.isEnabled('SMART_TASK')) {
      setOpen(true);
    } else {
      navigate({
        from: '/secured/$locale',
        to: '/secured/$locale/worker/tasks/create'
      })
    }
  }

  function handleClose() {
    setOpen(false);
  }

  return (
    <EveliPermissions id='CREATE_TASK'>
      <Button startIcon={<CreateOutlinedIcon />} className={classes.composeButton} onClick={handleCreateTask}>
        {intl.formatMessage({ id: 'button.compose' })}
      </Button>
      <AnyTaskRoute>
        <TaskCreate open={open} onClose={handleClose}/>
      </AnyTaskRoute>
    </EveliPermissions>
  )
}


export const Secondary: React.FC = () => {
  const intl = useIntl();
  const navigate = useNavigate();
  const location = useLocation()


  return (<>

    <EveliShellExplorer>
      <CreateTaskButton />

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
        <EveliPermissions id='NAV_TO_USER_PROFILE'>
          <Button startIcon={<PersonOutlineOutlinedIcon />}
            variant={location.pathname.endsWith('profile') ? 'explorerActive' : 'explorerInactive'}
            onClick={() => navigate({
              from: '/secured/$locale',
              to: '/secured/$locale/worker/profile'
            })}
          >
            {intl.formatMessage({ id: 'menu.profile' })}
          </Button>
        </EveliPermissions>
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

      <Button startIcon={<HealthAndSafetyOutlinedIcon />}
        variant={location.pathname.endsWith('health') ? 'explorerActive' : 'explorerInactive'}
        onClick={() => navigate({
          from: '/secured/$locale',
          to: '/secured/$locale/worker/health'
        })}
      >
        {intl.formatMessage({ id: 'menu.health', defaultMessage: 'Health' })}
      </Button>

      <Button startIcon={<SupervisedUserCircleOutlinedIcon />}
        variant={location.pathname.endsWith('user-activity') ? 'explorerActive' : 'explorerInactive'}
        onClick={() => navigate({
          from: '/secured/$locale',
          to: '/secured/$locale/worker/user-activity'
        })}
      >
        {intl.formatMessage({ id: 'menu.user-activity', defaultMessage: 'User activity' })}
      </Button>

    </EveliShellExplorer>
  </>
  )
}

