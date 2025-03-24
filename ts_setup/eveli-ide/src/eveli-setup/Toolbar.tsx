import React from 'react';
import { IconButton, Typography } from '@mui/material';
import HelpOutlineOutlinedIcon from '@mui/icons-material/HelpOutlineOutlined';
import ListIcon from '@mui/icons-material/ListAlt';
import SearchIcon from '@mui/icons-material/Search';
import TaskOutlinedIcon from '@mui/icons-material/TaskOutlined';
import BuildOutlinedIcon from '@mui/icons-material/BuildOutlined';
import EditNoteOutlinedIcon from '@mui/icons-material/EditNoteOutlined';
import SettingsOutlinedIcon from '@mui/icons-material/SettingsOutlined';

import { FormattedMessage } from 'react-intl';
import { useLocation, useNavigate } from '@tanstack/react-router'

import { EveliShellMiniBarClassName, EveliShellMiniBarRoot, useUtilityClasses } from '../eveli-shell/useUtilityClasses';

import * as Burger from '@/eveli-styles';


export const Toolbar: React.FC<{}> = () => {
  const navigate = useNavigate();
  const secondary = Burger.useIconbar();
  const classes = useUtilityClasses();
  const location = useLocation();

  const handleChange = (_event: React.SyntheticEvent, newValue: string) => {
    if (newValue === 'toolbar.search') {
      secondary.handleActiveId("toolbar.search")
    }
  };

  return (
    <EveliShellMiniBarRoot className={EveliShellMiniBarClassName} ownerState={{ unsaved: false }}>

      <div>
        <IconButton onClick={(event) => handleChange(event, 'toolbar.search')}><SearchIcon /></IconButton>
        <Typography><FormattedMessage id='toolbar.search' /></Typography>
      </div>

      <div>
        <IconButton {
          ...(location.pathname.endsWith('tasks') ? {
            disabled: true, 
            className: classes.itemActive
          } : {})
        }
        onClick={() => navigate({
          from: '/secured/$locale',
          to: '/secured/$locale/worker/tasks',
          search: { explorer: [] }
        })}>
          <TaskOutlinedIcon />
        </IconButton>
        <Typography {
          ...(location.pathname.endsWith('tasks') ? {
            className: classes.textActive
          } : {})
        }><FormattedMessage id='toolbar.tasks' /></Typography>
      </div>

      <div>
        <IconButton onClick={() => navigate({
          from: '/secured/$locale',
          to: '/secured/$locale/assets/wrench',
          search: { explorer: [] }
        })}>
          <BuildOutlinedIcon />
        </IconButton>
        <Typography><FormattedMessage id='toolbar.wrench' /></Typography>
      </div>

      <div>
        <IconButton onClick={() => navigate({
          from: '/secured/$locale',
          to: '/secured/$locale/assets/stencil',
          search: { explorer: [] }
        })}>
          <EditNoteOutlinedIcon />
        </IconButton>
        <Typography><FormattedMessage id='toolbar.stencil' /></Typography>
      </div>

      <div>
        <IconButton onClick={() => navigate({
          from: '/secured/$locale',
          to: '/secured/$locale/assets/services'
        })}>

          <SettingsOutlinedIcon />
        </IconButton>
        <Typography><FormattedMessage id='menu.workflows' /></Typography>
      </div>

      <div>
        <IconButton {
          ...(location.pathname.endsWith('forms') ? {
              disabled: true, 
              className: classes.itemActive
            } : {})
          }
          onClick={() => navigate({
            from: '/secured/$locale',
            to: '/secured/$locale/assets/forms'
          })}>

          <ListIcon />
        </IconButton>
        <Typography {
          ...(location.pathname.endsWith('forms') ? {
            className: classes.textActive
          } : {})
        }><FormattedMessage id='menu.forms' /></Typography>
      </div>

      <div>
        <IconButton onClick={() => window.open("https://github.com/the-stencil-io/the-stencil-composer/wiki", "_blank")}>
          <HelpOutlineOutlinedIcon />
        </IconButton>
        <Typography><FormattedMessage id='toolbar.help' /></Typography>
      </div>

      <Burger.EveliLocales />
    </EveliShellMiniBarRoot>
  );
}


