import React from 'react';
import { IconButton, Typography } from '@mui/material';
import HelpOutlineOutlinedIcon from '@mui/icons-material/HelpOutlineOutlined';
import LanguageIcon from '@mui/icons-material/Language';
import SearchIcon from '@mui/icons-material/Search';
import TaskOutlinedIcon from '@mui/icons-material/TaskOutlined';
import BuildOutlinedIcon from '@mui/icons-material/BuildOutlined';
import EditNoteOutlinedIcon from '@mui/icons-material/EditNoteOutlined';
import { FormattedMessage, useIntl } from 'react-intl';
import { useNavigate } from '@tanstack/react-router'

import { EveliShellMiniBarClassName, EveliShellMiniBarRoot, useUtilityClasses } from '../burger/eveli-shell/useUtilityClasses';
import { LocaleSelect } from '../uiDev';

import * as Burger from '@/burger';


export const Toolbar: React.FC<{}> = () => {
  const navigate = useNavigate();
  const secondary = Burger.useIconbar();
  const classes = useUtilityClasses();
  const { locale } = useIntl();

  const [anchorEl, setAnchorEl] = React.useState<HTMLButtonElement | null>(null);
  const handleLocalePopoverClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleLocalePopoverClose = () => {
    setAnchorEl(null);
  };

  const handleChange = (_event: React.SyntheticEvent, newValue: string) => {
    if (newValue === 'toolbar.search') {
      secondary.handleActiveId("toolbar.search")
    }
  };

  return (
    <EveliShellMiniBarRoot className={EveliShellMiniBarClassName} ownerState={{ unsaved: false }}>
      <LocaleSelect open={!!anchorEl} onClose={handleLocalePopoverClose} anchorEl={anchorEl} />
      <div>
        <IconButton onClick={(event) => handleChange(event, 'toolbar.search')}><SearchIcon /></IconButton>
        <Typography><FormattedMessage id='toolbar.search' /></Typography>
      </div>

      <div>
        <IconButton disabled className={classes.itemActive} onClick={() => { }}><TaskOutlinedIcon /></IconButton>
        <Typography><FormattedMessage id='toolbar.tasks' /></Typography>
      </div>

      <div>
        <IconButton onClick={() => navigate({
          from: '/secured/$locale',
          to: '/secured/$locale/assets/wrench'
        })}>
          <BuildOutlinedIcon />
        </IconButton>
        <Typography><FormattedMessage id='toolbar.wrench' /></Typography>
      </div>

      <div>
        <IconButton onClick={() => navigate({
          from: '/secured/$locale',
          to: '/secured/$locale/assets/stencil'
        })}>
          <EditNoteOutlinedIcon />
        </IconButton>
        <Typography><FormattedMessage id='toolbar.stencil' /></Typography>
      </div>

      <div>
        <IconButton onClick={() => window.open("https://github.com/the-stencil-io/the-stencil-composer/wiki", "_blank")}>
          <HelpOutlineOutlinedIcon />
        </IconButton>
        <Typography><FormattedMessage id='toolbar.help' /></Typography>
      </div>

      <div>
        <IconButton onClick={handleLocalePopoverClick}><LanguageIcon /></IconButton>
        <Typography>{locale.toLocaleUpperCase()}</Typography>
      </div>
    </EveliShellMiniBarRoot>
  );
}


