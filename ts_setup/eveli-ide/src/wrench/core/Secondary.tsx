import React from 'react';
import { Button } from '@mui/material';
import CreateOutlinedIcon from '@mui/icons-material/CreateOutlined';
import CodeOutlinedIcon from '@mui/icons-material/CodeOutlined';
import AccountTreeOutlinedIcon from '@mui/icons-material/AccountTreeOutlined';
import TableChartOutlinedIcon from '@mui/icons-material/TableChartOutlined';
import BugReportOutlinedIcon from '@mui/icons-material/BugReportOutlined';
import NewReleasesOutlinedIcon from '@mui/icons-material/NewReleasesOutlined';
import UploadFileOutlinedIcon from '@mui/icons-material/UploadFileOutlined';
import CompareArrowsOutlinedIcon from '@mui/icons-material/CompareArrowsOutlined';

import { useIntl } from 'react-intl';

import { ComposeSelect } from '../../uiDev/ComposeSelect';
import { useUtilityClasses } from '../../burger/eveli-shell/useUtilityClasses';
import logo from '../../uiDev/logoLifeDigitalDark.svg';
import * as Burger from '@/burger';
import { ServiceComposer } from './service';
import { MigrationComposer } from './migration/MigrationComposer';
import { useWrenchNav } from './nav';


export const Secondary: React.FC<{}> = () => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  const { onNav, activeItem } = useWrenchNav();

  const [anchorEl, setAnchorEl] = React.useState<HTMLButtonElement | null>(null);
  const [serviceComposerOpen, setServiceComposerOpen] = React.useState(false);
  const [migrationComposerOpen, setMigrationComposerOpen] = React.useState(false);

  function handleComposeSelectClick(event: React.MouseEvent<HTMLButtonElement>) {
    setAnchorEl(event.currentTarget);
  }

  function handleComposeSelectClose() {
    setAnchorEl(null);
  }

  return (
    <>
      <ComposeSelect open={!!anchorEl} anchorEl={anchorEl} onClose={handleComposeSelectClose} />
      {serviceComposerOpen && <ServiceComposer onClose={() => setServiceComposerOpen(false)} />}
      {migrationComposerOpen && <MigrationComposer onClose={() => setMigrationComposerOpen(false)} />}

      <Burger.EveliShellExplorer>
        <div className={classes.logoContainer}>
          <img src={logo} className={classes.logo} />
        </div>

        <Button startIcon={<CreateOutlinedIcon />} className={classes.composeButton} onClick={handleComposeSelectClick}>Compose</Button>

        <Button variant={activeItem?.type === 'FLOWS' ? 'explorerActive' : 'explorerInactive'}
          startIcon={<AccountTreeOutlinedIcon />}
          onClick={() => onNav({ type: 'FLOWS' })}>
          {intl.formatMessage({ id: 'menu.flows' })}
        </Button>

        <Button variant={activeItem?.type === 'DECISIONS' ? 'explorerActive' : 'explorerInactive'}
          startIcon={<TableChartOutlinedIcon />}
          onClick={() => onNav({ type: 'DECISIONS' })}>
          {intl.formatMessage({ id: 'menu.decisions' })}
        </Button>

        <Button variant={activeItem?.type === 'SERVICES' ? 'explorerActive' : 'explorerInactive'}
          startIcon={<CodeOutlinedIcon />}
          onClick={() => onNav({ type: 'SERVICES' })}>
          {intl.formatMessage({ id: 'menu.services' })}
        </Button>

        <Button variant={activeItem?.type === 'DEBUG' ? 'explorerActive' : 'explorerInactive'}
          startIcon={<BugReportOutlinedIcon />}
          onClick={() => onNav({ type: 'DEBUG' })}>
          {intl.formatMessage({ id: 'menu.debug' })}
        </Button>

        <Button variant={activeItem?.type === 'COMPARE' ? 'explorerActive' : 'explorerInactive'}
          startIcon={<CompareArrowsOutlinedIcon />}
          onClick={() => onNav({ type: 'COMPARE' })}>
          {intl.formatMessage({ id: 'menu.compare' })}
        </Button>

        <Button variant={activeItem?.type === 'MIGRATIONS' ? 'explorerActive' : 'explorerInactive'}
          startIcon={<UploadFileOutlinedIcon />}
          onClick={() => setMigrationComposerOpen(true)}>
          {intl.formatMessage({ id: 'menu.migrations' })}
        </Button>

        <Button variant={activeItem?.type === 'RELEASES' ? 'explorerActive' : 'explorerInactive'}
          startIcon={<NewReleasesOutlinedIcon />}
          onClick={() => onNav({ type: 'RELEASES' })}>
          {intl.formatMessage({ id: 'menu.releases' })}
        </Button>
      </Burger.EveliShellExplorer>
    </>
  )
}



