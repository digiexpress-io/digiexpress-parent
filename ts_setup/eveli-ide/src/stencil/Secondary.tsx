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
import { MigrationComposer } from './migration';
import { useStencilNav } from './nav';



const Secondary: React.FC<{}> = () => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  const userFirstAndLastName = 'Missing username';
  const { activeItem, onNav } = useStencilNav();
  const [anchorEl, setAnchorEl] = React.useState<HTMLButtonElement | null>(null);

  const [migrationsDialogOpen, setMigrationsDialogOpen] = React.useState(false)

  const handleComposeSelectClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  }
  const handleComposeSelectClose = () => {
    setAnchorEl(null);
  }

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
          className={activeItem?.type === 'ARTICLES' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => onNav('ARTICLES')}>
          {intl.formatMessage({ id: 'menu.articles' })}
        </Button>

        <Button variant='text' startIcon={<AccountTreeOutlinedIcon />}
          className={activeItem?.type === 'SERVICES' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => onNav('SERVICES')}>
          {intl.formatMessage({ id: 'menu.services' })}
        </Button>

        <Button variant='text' startIcon={<InsertLinkOutlinedIcon />}
          className={activeItem?.type === 'LINKS' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => onNav('LINKS')}>
          {intl.formatMessage({ id: 'menu.links' })}
        </Button>

        <Button variant='text' startIcon={<TranslateOutlinedIcon />}
          className={activeItem?.type === 'LOCALES' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => onNav('LOCALES')}>
          {intl.formatMessage({ id: 'menu.locales' })}
        </Button>

        <Button variant='text' startIcon={<FormatShapesOutlinedIcon />}
          className={activeItem?.type === 'TEMPLATES' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => onNav('TEMPLATES')}>
          {intl.formatMessage({ id: 'menu.templates' })}
        </Button>

        <Button variant='text' startIcon={<UploadFileOutlinedIcon />}
          className={activeItem?.type === 'MIGRATIONS' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => setMigrationsDialogOpen(true)}>
          {intl.formatMessage({ id: 'menu.migrations' })}
        </Button>

        <Button variant='text' startIcon={<NewReleasesOutlinedIcon />}
          className={activeItem?.type === 'RELEASES' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => onNav('RELEASES')}>
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


