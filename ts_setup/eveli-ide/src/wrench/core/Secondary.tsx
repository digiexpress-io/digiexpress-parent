import React from 'react';
import { Typography, Button, Divider, Stack } from '@mui/material';
import CreateOutlinedIcon from '@mui/icons-material/CreateOutlined';
import CodeOutlinedIcon from '@mui/icons-material/CodeOutlined';
import AccountTreeOutlinedIcon from '@mui/icons-material/AccountTreeOutlined';
import TableChartOutlinedIcon from '@mui/icons-material/TableChartOutlined';
import BugReportOutlinedIcon from '@mui/icons-material/BugReportOutlined';
import LogoutIcon from '@mui/icons-material/Logout';
import NewReleasesOutlinedIcon from '@mui/icons-material/NewReleasesOutlined';
import UploadFileOutlinedIcon from '@mui/icons-material/UploadFileOutlined';
import FormatShapesOutlinedIcon from '@mui/icons-material/FormatShapesOutlined';
import CompareArrowsOutlinedIcon from '@mui/icons-material/CompareArrowsOutlined';

import { useIntl } from 'react-intl';

import { ComposeSelect } from '../../uiDev/ComposeSelect';
import { EveliShellLargeBarRoot, useUtilityClasses } from '../../burger/eveli-shell/useUtilityClasses';
import logo from '../../uiDev/logoLifeDigitalDark.svg';


type NavType = 'FLOWS' | 'SERVICES' | 'DECISIONS' | 'DEBUG' | 'RELEASES' | 'COMPARE' | 'TEMPLATES' | 'MIGRATIONS';

export const Secondary: React.FC<{}> = () => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const userFirstAndLastName = 'Missing username';


  const [anchorEl, setAnchorEl] = React.useState<HTMLButtonElement | null>(null);
  const [activeButton, setActiveButton] = React.useState<NavType>('FLOWS')

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

      <EveliShellLargeBarRoot className={classes.root}>
        <div className={classes.logoContainer}>
          <img src={logo} className={classes.logo} />
        </div>

        <Button startIcon={<CreateOutlinedIcon />} className={classes.composeButton} onClick={handleComposeSelectClick}>Compose</Button>

        <Button variant='text' startIcon={<AccountTreeOutlinedIcon />}
          className={activeButton === 'FLOWS' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => handleMenuButtonClick('FLOWS')}>
          {intl.formatMessage({ id: 'menu.flows' })}
        </Button>

        <Button variant='text' startIcon={<TableChartOutlinedIcon />}
          className={activeButton === 'DECISIONS' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => handleMenuButtonClick('DECISIONS')}>
          {intl.formatMessage({ id: 'menu.decisions' })}
        </Button>

        <Button variant='text' startIcon={<CodeOutlinedIcon />}
          className={activeButton === 'SERVICES' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => handleMenuButtonClick('SERVICES')}>
          {intl.formatMessage({ id: 'menu.services' })}
        </Button>

        <Button variant='text' startIcon={<BugReportOutlinedIcon />}
          className={activeButton === 'DEBUG' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => handleMenuButtonClick('DEBUG')}>
          {intl.formatMessage({ id: 'menu.debug' })}
        </Button>

        <Button variant='text' startIcon={<CompareArrowsOutlinedIcon />}
          className={activeButton === 'COMPARE' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => handleMenuButtonClick('COMPARE')}>
          {intl.formatMessage({ id: 'menu.compare' })}
        </Button>

        <Button variant='text' startIcon={<FormatShapesOutlinedIcon />}
          className={activeButton === 'TEMPLATES' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => handleMenuButtonClick('TEMPLATES')}>
          {intl.formatMessage({ id: 'menu.templates' })}
        </Button>

        <Button variant='text' startIcon={<UploadFileOutlinedIcon />}
          className={activeButton === 'MIGRATIONS' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => handleMenuButtonClick('MIGRATIONS')}>
          {intl.formatMessage({ id: 'menu.migrations' })}
        </Button>

        <Button variant='text' startIcon={<NewReleasesOutlinedIcon />}
          className={activeButton === 'RELEASES' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => handleMenuButtonClick('RELEASES')}>
          {intl.formatMessage({ id: 'menu.releases' })}
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



