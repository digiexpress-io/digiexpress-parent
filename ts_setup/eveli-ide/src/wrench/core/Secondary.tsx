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


type NavType = 'FLOWS' | 'SERVICES' | 'DECISIONS' | 'DEBUG' | 'RELEASES' | 'COMPARE' | 'TEMPLATES' | 'MIGRATIONS';

export const Secondary: React.FC<{}> = () => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const tabs = Burger.useTabs();


  const [anchorEl, setAnchorEl] = React.useState<HTMLButtonElement | null>(null);
  const [activeButton, setActiveButton] = React.useState<NavType>('FLOWS')
  const [serviceComposerOpen, setServiceComposerOpen] = React.useState(false);

  const handleComposeSelectClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleComposeSelectClose = () => {
    setAnchorEl(null);
  };

  function handleMenuButtonClick(buttonId: NavType) {
    setActiveButton(buttonId)
  }


  return (
    <>
      <ComposeSelect open={!!anchorEl} anchorEl={anchorEl} onClose={handleComposeSelectClose} />
      {serviceComposerOpen && <ServiceComposer onClose={() => setServiceComposerOpen(false)} />}

      <Burger.EveliShellExplorer>
        <div className={classes.logoContainer}>
          <img src={logo} className={classes.logo} />
        </div>

        <Button startIcon={<CreateOutlinedIcon />} className={classes.composeButton} onClick={handleComposeSelectClick}>Compose</Button>

        <Button variant='text' startIcon={<AccountTreeOutlinedIcon />}
          className={activeButton === 'FLOWS' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => tabs.handleTabAdd({ id: 'flows', label: "Flows" })}>
          {intl.formatMessage({ id: 'menu.flows' })}
        </Button>

        <Button variant='text' startIcon={<TableChartOutlinedIcon />}
          className={activeButton === 'DECISIONS' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => tabs.handleTabAdd({ id: 'decisions', label: "Decisions" })}>
          {intl.formatMessage({ id: 'menu.decisions' })}
        </Button>

        <Button variant='text' startIcon={<CodeOutlinedIcon />}
          className={activeButton === 'SERVICES' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => setServiceComposerOpen(true)}>
          {intl.formatMessage({ id: 'menu.services' })}
        </Button>

        <Button variant='text' startIcon={<BugReportOutlinedIcon />}
          className={activeButton === 'DEBUG' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => tabs.handleTabAdd({ id: 'debug', label: "Debug" })}>
          {intl.formatMessage({ id: 'menu.debug' })}
        </Button>

        <Button variant='text' startIcon={<CompareArrowsOutlinedIcon />}
          className={activeButton === 'COMPARE' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => tabs.handleTabAdd({ id: 'compare', label: "Compare" })}>
          {intl.formatMessage({ id: 'menu.compare' })}
        </Button>

        {/*
         <Button variant='text' startIcon={<FormatShapesOutlinedIcon />}
          className={activeButton === 'TEMPLATES' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => tabs.handleTabAdd({ id: 'templates', label: "Templates" })}>
          {intl.formatMessage({ id: 'menu.templates' })}
         </Button>

       */}

        <Button variant='text' startIcon={<UploadFileOutlinedIcon />}
          className={activeButton === 'MIGRATIONS' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => handleMenuButtonClick('MIGRATIONS')}>
          {intl.formatMessage({ id: 'menu.migrations' })}
        </Button>

        <Button variant='text' startIcon={<NewReleasesOutlinedIcon />}
          className={activeButton === 'RELEASES' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => tabs.handleTabAdd({ id: 'releases', label: "Releases" })}>
          {intl.formatMessage({ id: 'menu.releases' })}
        </Button>
      </Burger.EveliShellExplorer>
    </>
  )
}



