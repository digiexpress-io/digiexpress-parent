import React from 'react';
import { Button, Divider, Stack, Typography } from '@mui/material';
import CreateOutlinedIcon from '@mui/icons-material/CreateOutlined';
import MenuBookOutlinedIcon from '@mui/icons-material/MenuBookOutlined';
import AccountTreeOutlinedIcon from '@mui/icons-material/AccountTreeOutlined';
import InsertLinkOutlinedIcon from '@mui/icons-material/InsertLinkOutlined';
import TranslateOutlinedIcon from '@mui/icons-material/TranslateOutlined';
import UploadFileOutlinedIcon from '@mui/icons-material/UploadFileOutlined';
import FormatShapesOutlinedIcon from '@mui/icons-material/FormatShapesOutlined';
import LogoutIcon from '@mui/icons-material/Logout';
import NewReleasesOutlinedIcon from '@mui/icons-material/NewReleasesOutlined';

import { useIntl } from 'react-intl';

import { EveliShellLargeBarRoot, useUtilityClasses } from '../burger/eveli-shell/useUtilityClasses';
import { ComposeSelect } from '../uiDev/ComposeSelect';
import logo from '../uiDev/logoLifeDigitalDark.svg';
import * as Burger from '@/burger';
import { MigrationComposer } from './migration';


type NavType = 'ARTICLES' | 'PAGES' | 'SERVICES' | 'LINKS' | 'LOCALES' | 'MIGRATIONS' | 'TEMPLATES' | 'RELEASES';


const Secondary: React.FC<{}> = () => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const tabs = Burger.useTabs();

  const userFirstAndLastName = 'Missing username';

  const [anchorEl, setAnchorEl] = React.useState<HTMLButtonElement | null>(null);
  const [activeButton, setActiveButton] = React.useState<NavType>('ARTICLES');
  const [migrationsDialogOpen, setMigrationsDialogOpen] = React.useState(false)

  console.log("tabs", tabs)
  const handleComposeSelectClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleComposeSelectClose = () => {
    setAnchorEl(null);
  };


  return (
    <>
      <ComposeSelect open={!!anchorEl} anchorEl={anchorEl} onClose={handleComposeSelectClose} />
      {migrationsDialogOpen && <MigrationComposer onClose={() => setMigrationsDialogOpen(false)} />}

      <EveliShellLargeBarRoot className={classes.root}>
        <div className={classes.logoContainer}>
          <img src={logo} className={classes.logo} />
        </div>

        <Button startIcon={<CreateOutlinedIcon />}
          className={classes.composeButton}
          onClick={handleComposeSelectClick}>
          {intl.formatMessage({ id: 'menu.compose' })}
        </Button>

        <Button variant='text' startIcon={<MenuBookOutlinedIcon />}
          className={activeButton === 'ARTICLES' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => tabs.handleTabAdd({ id: 'articles', label: "Articles" })}>
          {intl.formatMessage({ id: 'menu.articles' })}
        </Button>

        <Button variant='text' startIcon={<AccountTreeOutlinedIcon />}
          className={activeButton === 'SERVICES' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => tabs.handleTabAdd({ id: 'workflows', label: "Workflows" })}>
          {intl.formatMessage({ id: 'menu.services' })}
        </Button>

        <Button variant='text' startIcon={<InsertLinkOutlinedIcon />}
          className={activeButton === 'LINKS' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => tabs.handleTabAdd({ id: 'links', label: "Links" })}>
          {intl.formatMessage({ id: 'menu.links' })}
        </Button>

        <Button variant='text' startIcon={<TranslateOutlinedIcon />}
          className={activeButton === 'LOCALES' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => tabs.handleTabAdd({ id: 'locales', label: "Locales" })}>
          {intl.formatMessage({ id: 'menu.locales' })}
        </Button>

        <Button variant='text' startIcon={<FormatShapesOutlinedIcon />}
          className={activeButton === 'TEMPLATES' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => tabs.handleTabAdd({ id: 'templates', label: "Templates" })}>
          {intl.formatMessage({ id: 'menu.templates' })}
        </Button>

        <Button variant='text' startIcon={<UploadFileOutlinedIcon />}
          className={activeButton === 'MIGRATIONS' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => setMigrationsDialogOpen(true)}>
          {intl.formatMessage({ id: 'menu.migrations' })}
        </Button>

        <Button variant='text' startIcon={<NewReleasesOutlinedIcon />}
          className={activeButton === 'RELEASES' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => tabs.handleTabAdd({ id: 'releases', label: "Releases" })}>
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
export { Secondary }


