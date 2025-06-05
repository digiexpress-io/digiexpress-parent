import React from 'react';
import { Button, ListItemText, MenuItem, MenuList } from '@mui/material';
import CreateOutlinedIcon from '@mui/icons-material/CreateOutlined';
import CodeOutlinedIcon from '@mui/icons-material/CodeOutlined';
import AccountTreeOutlinedIcon from '@mui/icons-material/AccountTreeOutlined';
import TableChartOutlinedIcon from '@mui/icons-material/TableChartOutlined';
import BugReportOutlinedIcon from '@mui/icons-material/BugReportOutlined';
import NewReleasesOutlinedIcon from '@mui/icons-material/NewReleasesOutlined';
import CompareArrowsOutlinedIcon from '@mui/icons-material/CompareArrowsOutlined';
import HelpOutlineOutlinedIcon from '@mui/icons-material/HelpOutlineOutlined';
import DashboardCustomizeOutlinedIcon from '@mui/icons-material/DashboardCustomizeOutlined';

import { useIntl } from 'react-intl';


import { useUtilityClasses } from '../eveli-shell/useUtilityClasses';
import { useWrenchNav } from '../wrench-nav';
import { useActivities, ActivityProps } from './Activities';
import { EveliShellCompose } from '@/eveli-shell-compose';
import { EveliShellExplorer } from '@/eveli-shell-explorer';
import { EveliPermissions } from '@/eveli-permissions';
import { EveliTenantFeatureEnabled } from '@/api-tenant-config';




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
          {intl.formatMessage({ id: 'menu.compose' })}
        </Button>
        </EveliPermissions>

        <EveliPermissions id='NAV_TO_WRENCH_FLOWS'>
        <Button variant={activeItem?.type === 'FLOWS' ? 'explorerActive' : 'explorerInactive'}
          startIcon={<AccountTreeOutlinedIcon />}
          onClick={() => onNav({ type: 'FLOWS' })}>
          {intl.formatMessage({ id: 'menu.flows' })}
        </Button>
        </EveliPermissions>

        <EveliPermissions id='NAV_TO_WRENCH_DECISIONS'>
        <Button variant={activeItem?.type === 'DECISIONS' ? 'explorerActive' : 'explorerInactive'}
          startIcon={<TableChartOutlinedIcon />}
          onClick={() => onNav({ type: 'DECISIONS' })}>
          {intl.formatMessage({ id: 'menu.decisions' })}
        </Button>
        </EveliPermissions>

        <EveliPermissions id='NAV_TO_WRENCH_SERVICES'>
        <Button variant={activeItem?.type === 'SERVICES' ? 'explorerActive' : 'explorerInactive'}
          startIcon={<CodeOutlinedIcon />}
          onClick={() => onNav({ type: 'SERVICES' })}>
          {intl.formatMessage({ id: 'menu.services' })}
        </Button>
        </EveliPermissions>

        <EveliPermissions id='NAV_TO_WRENCH_DEBUG'>
          <Button variant={activeItem?.type === 'DEBUG' ? 'explorerActive' : 'explorerInactive'}
            startIcon={<BugReportOutlinedIcon />}
            onClick={() => onNav({ type: 'DEBUG' })}>
            {intl.formatMessage({ id: 'menu.debug' })}
          </Button>
        </EveliPermissions>

        <EveliPermissions id='NAV_TO_WRENCH_COMPARE'>
          <Button variant={activeItem?.type === 'COMPARE' ? 'explorerActive' : 'explorerInactive'}
            startIcon={<CompareArrowsOutlinedIcon />}
            onClick={() => onNav({ type: 'COMPARE' })}>
            {intl.formatMessage({ id: 'menu.compare' })}
          </Button>
        </EveliPermissions>

        <EveliTenantFeatureEnabled id='WRENCH_RELEASES'>
          <EveliPermissions id='NAV_TO_WRENCH_RELEASES'>
            <Button variant={activeItem?.type === 'RELEASES' ? 'explorerActive' : 'explorerInactive'}
              startIcon={<NewReleasesOutlinedIcon />}
              onClick={() => onNav({ type: 'RELEASES' })}>
              {intl.formatMessage({ id: 'menu.releases' })}
            </Button>
          </EveliPermissions>
        </EveliTenantFeatureEnabled>

        <Button variant={activeItem?.type === 'ACTIVITIES' ? 'explorerActive' : 'explorerInactive'}
          startIcon={<DashboardCustomizeOutlinedIcon />}
          onClick={() => onNav({ type: 'ACTIVITIES' })}>
          {intl.formatMessage({ id: 'menu.activities' })}
        </Button>

        <Button variant='explorerInactive'
          startIcon={<HelpOutlineOutlinedIcon />}
          onClick={() => window.open("https://github.com/the-wrench-io/hdes-parent/wiki", "_blank")}>
          {intl.formatMessage({ id: 'menu.help' })}
        </Button>
      </EveliShellExplorer>
    </>
  )
}



