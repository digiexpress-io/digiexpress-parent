import React from 'react';
import { IconButton, Typography } from '@mui/material';
import HelpOutlineOutlinedIcon from '@mui/icons-material/HelpOutlineOutlined';
import ListIcon from '@mui/icons-material/ListAlt';
import TaskOutlinedIcon from '@mui/icons-material/TaskOutlined';
import BuildOutlinedIcon from '@mui/icons-material/BuildOutlined';
import EditNoteOutlinedIcon from '@mui/icons-material/EditNoteOutlined';
import BeenhereOutlinedIcon from '@mui/icons-material/BeenhereOutlined';

import { FormattedMessage } from 'react-intl';
import { useLocation, useNavigate } from '@tanstack/react-router'

import { EveliShellMiniBarClassName, EveliShellMiniBarRoot, useUtilityClasses } from '../eveli-shell/useUtilityClasses';


import { EveliLocales } from '@/eveli-locales';


export const Toolbar: React.FC<{  }> = ({  }) => {
  const navigate = useNavigate();
  const classes = useUtilityClasses();
  const location = useLocation();

  return (
    <EveliShellMiniBarRoot className={EveliShellMiniBarClassName} ownerState={{ unsaved: false }}>
      <div>
        <IconButton { ...(location.pathname.includes('tasks') ? { disabled: true, className: classes.itemActive} : {}) }
          onClick={() => navigate({
            from: '/secured/$locale',
            to: '/secured/$locale/worker/tasks',
            search: { explorer: [] }
          })}>
          <TaskOutlinedIcon />
        </IconButton>
        <Typography { ...(location.pathname.includes('tasks') ? { className: classes.textActive } : {})}>
          <FormattedMessage id='toolbar.tasks' />
        </Typography>
      </div>

      <div>
        <IconButton 
          { ...(location.pathname.includes('wrench') ? { disabled: true, className: classes.itemActive} : {}) }
          onClick={() => navigate({
            from: '/secured/$locale',
            to: '/secured/$locale/assets/wrench',
            search: { explorer: [] }
          })}>
          <BuildOutlinedIcon />
        </IconButton>
        <Typography><FormattedMessage id='toolbar.wrench' /></Typography>
      </div>

      <div>
        <IconButton 
          { ...(location.pathname.includes('stencil') ? { disabled: true, className: classes.itemActive} : {}) }
          onClick={() => navigate({
            from: '/secured/$locale',
            to: '/secured/$locale/assets/stencil',
            search: { explorer: [] }
          })}>
          <EditNoteOutlinedIcon />
        </IconButton>
        <Typography><FormattedMessage id='toolbar.stencil' /></Typography>
      </div>

      <div>
        <IconButton { ...(location.pathname.endsWith('forms') ? { disabled: true, className: classes.itemActive } : {}) }
          onClick={() => navigate({
            from: '/secured/$locale',
            to: '/secured/$locale/assets/forms'
          })}>
          <ListIcon />
        </IconButton>
        <Typography { ...(location.pathname.endsWith('forms') ? { className: classes.textActive } : {})
        }>
          <FormattedMessage id='menu.forms' />
        </Typography>
      </div>

      <div>
        <IconButton { ...(location.pathname.endsWith('publications') ? { disabled: true, className: classes.itemActive } : {}) }
          onClick={() => navigate({
            from: '/secured/$locale',
            to: '/secured/$locale/publications'
          })}>

          <BeenhereOutlinedIcon />
        </IconButton>
      
        <Typography {...(location.pathname.endsWith('publications') ? { className: classes.textActive } : {})}>
          <FormattedMessage id='menu.publications' />
        </Typography>      
      </div>


      <div>
        <IconButton 
          onClick={() => window.open("https://github.com/digiexpress-io/digiexpress-parent/wiki", "_blank")}>
          
          <HelpOutlineOutlinedIcon />
        </IconButton>
        <Typography><FormattedMessage id='toolbar.help' /></Typography>
      </div>

      <EveliLocales />

    </EveliShellMiniBarRoot>
  );
}


