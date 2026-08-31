import React from 'react';
import { Button, ListItemText, MenuItem, MenuList } from '@mui/material';
import { CreateOutlined as CreateOutlinedIcon } from '@mui/icons-material';
import { CodeOutlined as CodeOutlinedIcon } from '@mui/icons-material';
import { AccountTreeOutlined as AccountTreeOutlinedIcon } from '@mui/icons-material';
import { TableChartOutlined as TableChartOutlinedIcon } from '@mui/icons-material';
import { BugReportOutlined as BugReportOutlinedIcon } from '@mui/icons-material';
import { NewReleasesOutlined as NewReleasesOutlinedIcon } from '@mui/icons-material';
import { CompareArrowsOutlined as CompareArrowsOutlinedIcon } from '@mui/icons-material';
import { HelpOutlineOutlined as HelpOutlineOutlinedIcon } from '@mui/icons-material';

import { useIntl } from 'react-intl';
import { EveliTenantFeatureEnabled } from '@dxs-ts/eveli-api';
import { EveliPermissions, EveliShellExplorer, EveliShellCompose, _eveli_shell_useUtilityClasses as useUtilityClasses } from '@dxs-ts/eveli-primitives';

import { useWrenchNav } from '../wrench-nav';
import { useActivities, ActivityProps } from './Activities';



const ActivitiesViewItem: React.FC<{ data: ActivityProps, onClick: () => void }> = (props) => {
  const [open, setOpen] = React.useState<boolean>(false);
  const handleClose = () => { 
    setOpen(false) 
    //props.onClick();
  };
  const handleOpen = () => {
    setOpen(true);
  }
  const Composer: React.FC< {onClose: () => void}> = open === false ? () => (<></>) : props.data.composer;
  return (
    <>
      <Composer onClose={handleClose}/>
      <MenuItem onClick={handleOpen}>
        <ListItemText>{props.data.buttonCreate} {props.data.title}</ListItemText>
      </MenuItem>
    </>
  )
}


export const Secondary: React.FC<{}> = () => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  const { onNav, activeItem } = useWrenchNav();
  const activities = useActivities();

  const [anchorEl, setAnchorEl] = React.useState<HTMLButtonElement | null>(null);

  function handleComposeSelectClick(event: React.MouseEvent<HTMLButtonElement>) {
    setAnchorEl(event.currentTarget);
  }

  function handleComposeSelectClose() {
    setAnchorEl(null);
  }

  return (
    <>
      
      <EveliShellCompose open={!!anchorEl} anchorEl={anchorEl} onClose={handleComposeSelectClose}>
        <MenuList>
          {activities.map((activity, index) => (<ActivitiesViewItem key={index} data={activity} onClick={handleComposeSelectClose}/>))}
        </MenuList>
      </EveliShellCompose>

      <EveliShellExplorer>
        <EveliPermissions id='CREATE_WRENCH_ASSET'>
        <Button startIcon={<CreateOutlinedIcon />}
          className={classes.composeButton}
          onClick={handleComposeSelectClick}>
          {intl.formatMessage({ id: 'menu.compose.wrench' })}
        </Button>
        </EveliPermissions>

        <EveliPermissions id='NAV_TO_WRENCH_FLOWS'>
        <Button variant={activeItem?.type === 'FLOWS' ? 'explorerActive' : 'explorerInactive'}
          startIcon={<AccountTreeOutlinedIcon />}
          onClick={() => onNav({ type: 'FLOWS' })}>
          {intl.formatMessage({ id: 'menu.flows.wrench' })}
        </Button>
        </EveliPermissions>

        <EveliPermissions id='NAV_TO_WRENCH_DECISIONS'>
        <Button variant={activeItem?.type === 'DECISIONS' ? 'explorerActive' : 'explorerInactive'}
          startIcon={<TableChartOutlinedIcon />}
          onClick={() => onNav({ type: 'DECISIONS' })}>
          {intl.formatMessage({ id: 'menu.decisions.wrench' })}
        </Button>
        </EveliPermissions>

        <EveliPermissions id='NAV_TO_WRENCH_SERVICES'>
        <Button variant={activeItem?.type === 'SERVICES' ? 'explorerActive' : 'explorerInactive'}
          startIcon={<CodeOutlinedIcon />}
          onClick={() => onNav({ type: 'SERVICES' })}>
          {intl.formatMessage({ id: 'menu.services.wrench' })}
        </Button>
        </EveliPermissions>

        <EveliPermissions id='NAV_TO_WRENCH_DEBUG'>
          <Button variant={activeItem?.type === 'DEBUG' ? 'explorerActive' : 'explorerInactive'}
            startIcon={<BugReportOutlinedIcon />}
            onClick={() => onNav({ type: 'DEBUG' })}>
            {intl.formatMessage({ id: 'menu.debug.wrench' })}
          </Button>
        </EveliPermissions>

        <EveliPermissions id='NAV_TO_WRENCH_COMPARE'>
          <Button variant={activeItem?.type === 'COMPARE' ? 'explorerActive' : 'explorerInactive'}
            startIcon={<CompareArrowsOutlinedIcon />}
            onClick={() => onNav({ type: 'COMPARE' })}>
            {intl.formatMessage({ id: 'menu.compare.wrench' })}
          </Button>
        </EveliPermissions>
        
        <Button variant='explorerInactive'
          startIcon={<HelpOutlineOutlinedIcon />}
          onClick={() => window.open("https://github.com/the-wrench-io/hdes-parent/wiki", "_blank")}>
          {intl.formatMessage({ id: 'menu.help' })}
        </Button>
      </EveliShellExplorer>
    </>
  )
}



