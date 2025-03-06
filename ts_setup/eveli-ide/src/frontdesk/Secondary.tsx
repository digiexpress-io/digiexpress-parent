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
import LogoutIcon from '@mui/icons-material/Logout';

import { useIntl } from 'react-intl';
import { useNavigate } from '@tanstack/react-router'


import { EveliShellLargeBarRoot, useUtilityClasses } from '../burger/eveli-shell/useUtilityClasses';

import logo from '../uiDev/logoLifeDigitalDark.svg';
import { ComposeSelect } from '../uiDev/ComposeSelect';

type NavType = 'TASKS' | 'DASHBOARD' | 'PROCESSES' | 'FORMS' | 'WRENCH' | 'STENCIL' | 'WORKFLOWS' | 'FEEDBACK' | 'QUEUES' | 'PUBLICATIONS';

const navPaths: Record<NavType, string> = {
  TASKS: '/ui/tasks',
  DASHBOARD: '/ui/dashboard',
  PROCESSES: '/ui/processes',
  FORMS: '/ui/forms',
  WRENCH: '/wrench/ide',
  STENCIL: '/ui/content',
  WORKFLOWS: '/ui/workflows',
  FEEDBACK: '/feedback',
  QUEUES: './queues',
  PUBLICATIONS: '/ui/publications'
}


export const Secondary: React.FC = () => {
  const intl = useIntl();
  const navigate = useNavigate();
  const classes = useUtilityClasses();

  const userFirstAndLastName = 'Missing username';

  const [anchorEl, setAnchorEl] = React.useState<HTMLButtonElement | null>(null);
  const [activeButton, setActiveButton] = React.useState<NavType>('TASKS')

  const handleComposeSelectClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleComposeSelectClose = () => {
    setAnchorEl(null);
  };

  function navigateTo(navType: NavType) {
    const path = navPaths[navType] || '/';
    //navigate(path)
  }

  function handleMenuButtonClick(buttonId: NavType) {
    setActiveButton(buttonId),
      navigateTo(buttonId)
  }

  return (<>
    <ComposeSelect open={!!anchorEl} anchorEl={anchorEl} onClose={handleComposeSelectClose} />

    <EveliShellLargeBarRoot className={classes.root}>
      <div className={classes.logoContainer}>
        <img src={logo} className={classes.logo} />
      </div>

      <Button startIcon={<CreateOutlinedIcon />} className={classes.composeButton} onClick={handleComposeSelectClick}>Compose</Button>

      <Button variant='text' startIcon={<TaskOutlinedIcon />}
        className={activeButton === 'TASKS' ? classes.menuButtonActive : classes.menuButton}
        onClick={() => handleMenuButtonClick('TASKS')}>
        {intl.formatMessage({ id: 'menu.tasks' })}
      </Button>

      <Button variant='text' startIcon={<DashboardCustomizeOutlinedIcon />}
        className={activeButton === 'DASHBOARD' ? classes.menuButtonActive : classes.menuButton}
        onClick={() => handleMenuButtonClick('DASHBOARD')}>
        {intl.formatMessage({ id: 'menu.dashboard' })}
      </Button>

      <Button variant='text' startIcon={<NetworkCheckIcon />}
        className={activeButton === 'PROCESSES' ? classes.menuButtonActive : classes.menuButton}
        onClick={() => handleMenuButtonClick('PROCESSES')}>
        {intl.formatMessage({ id: 'menu.processes' })}
      </Button>

      <Button variant='text' startIcon={<ListIcon />}
        className={activeButton === 'FORMS' ? classes.menuButtonActive : classes.menuButton}
        onClick={() => handleMenuButtonClick('FORMS')}>
        {intl.formatMessage({ id: 'menu.forms' })}
      </Button>

      <Button variant='text' startIcon={<BuildOutlinedIcon />}
        className={activeButton === 'WRENCH' ? classes.menuButtonActive : classes.menuButton}
        onClick={() => handleMenuButtonClick('WRENCH')}>
        {intl.formatMessage({ id: 'menu.flow' })}
      </Button>

      <Button variant='text' startIcon={<EditNoteOutlinedIcon />}
        className={activeButton === 'STENCIL' ? classes.menuButtonActive : classes.menuButton}
        onClick={() => handleMenuButtonClick('STENCIL')}>
        {intl.formatMessage({ id: 'menu.content' })}
      </Button>

      <Button variant='text' startIcon={<SettingsOutlinedIcon />}
        className={activeButton === 'WORKFLOWS' ? classes.menuButtonActive : classes.menuButton}
        onClick={() => handleMenuButtonClick('WORKFLOWS')}>
        {intl.formatMessage({ id: 'menu.workflows' })}
      </Button>

      <Button variant='text' startIcon={<ThumbUpAltOutlinedIcon />}
        className={activeButton === 'FEEDBACK' ? classes.menuButtonActive : classes.menuButton}
        onClick={() => handleMenuButtonClick('FEEDBACK')}>
        {intl.formatMessage({ id: 'menu.feedback' })}
      </Button>

      <Button variant='text' startIcon={<CloudQueueIcon />}
        className={activeButton === 'QUEUES' ? classes.menuButtonActive : classes.menuButton}
        onClick={() => handleMenuButtonClick('QUEUES')}>
        {intl.formatMessage({ id: 'menu.queues' })}
      </Button>

      <Button variant='text' startIcon={<BeenhereOutlinedIcon />}
        className={activeButton === 'PUBLICATIONS' ? classes.menuButtonActive : classes.menuButton}
        onClick={() => handleMenuButtonClick('PUBLICATIONS')}>
        {intl.formatMessage({ id: 'menu.publications' })}
      </Button>

      <Divider className={classes.secondaryDivider} />

      <Button className={classes.logoutButton}
        variant="text"
        startIcon={<LogoutIcon />}
        onClick={() => console.log("log out")}
      >
        <Stack spacing={0} alignItems="flex-start">
          <Typography>{intl.formatMessage({ id: 'menu.logout' })}</Typography>
          <Typography variant="caption">{userFirstAndLastName}</Typography>
        </Stack>
      </Button>

    </EveliShellLargeBarRoot>
  </>
  )
}

