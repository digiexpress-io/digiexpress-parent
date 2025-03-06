import React from 'react';
import { Button, Divider, Stack, Typography } from '@mui/material';
import CreateOutlinedIcon from '@mui/icons-material/CreateOutlined';
import MenuBookOutlinedIcon from '@mui/icons-material/MenuBookOutlined';
import AccountTreeOutlinedIcon from '@mui/icons-material/AccountTreeOutlined';
import InsertLinkOutlinedIcon from '@mui/icons-material/InsertLinkOutlined';
import TranslateOutlinedIcon from '@mui/icons-material/TranslateOutlined';
import DescriptionOutlinedIcon from '@mui/icons-material/DescriptionOutlined';
import UploadFileOutlinedIcon from '@mui/icons-material/UploadFileOutlined';
import FormatShapesOutlinedIcon from '@mui/icons-material/FormatShapesOutlined';
import LogoutIcon from '@mui/icons-material/Logout';
import NewReleasesOutlinedIcon from '@mui/icons-material/NewReleasesOutlined';

import { useIntl } from 'react-intl';

import { EveliShellLargeBarRoot, useUtilityClasses } from '../burger/eveli-shell/useUtilityClasses';
import { ComposeSelect } from '../uiDev/ComposeSelect';
import logo from '../uiDev/logoLifeDigitalDark.svg';
import { useNavigate } from '@tanstack/react-router';


type NavType = 'ARTICLES' | 'PAGES' | 'SERVICES' | 'LINKS' | 'LOCALES' | 'MIGRATIONS' | 'TEMPLATES' | 'RELEASES';


const Secondary: React.FC<{}> = () => {
  const intl = useIntl();
  const navigate = useNavigate();
  const classes = useUtilityClasses();


  const userFirstAndLastName = 'Missing username';

  const [anchorEl, setAnchorEl] = React.useState<HTMLButtonElement | null>(null);
  const [activeButton, setActiveButton] = React.useState<NavType>('ARTICLES')

  const handleComposeSelectClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleComposeSelectClose = () => {
    setAnchorEl(null);
  };

  function navigateTo(navType: NavType) {
    /*navigate({
      from: '/secured/$locale/assets/stencil',
      to: ''
    });
    */
  }

  function handleMenuButtonClick(buttonId: NavType) {
    setActiveButton(buttonId)
    // navigateTo(buttonId)
  }
  return (
    <>
      <ComposeSelect open={!!anchorEl} anchorEl={anchorEl} onClose={handleComposeSelectClose} />

      <EveliShellLargeBarRoot className={classes.root}>
        <div className={classes.logoContainer}>
          <img src={logo} className={classes.logo} />
        </div>

        <Button startIcon={<CreateOutlinedIcon />} className={classes.composeButton} onClick={handleComposeSelectClick}>Compose</Button>

        <Button variant='text' startIcon={<MenuBookOutlinedIcon />}
          className={activeButton === 'ARTICLES' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => handleMenuButtonClick('ARTICLES')}>
          {intl.formatMessage({ id: 'menu.articles' })}
        </Button>

        <Button variant='text' startIcon={<DescriptionOutlinedIcon />}
          className={activeButton === 'PAGES' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => handleMenuButtonClick('PAGES')}>
          {intl.formatMessage({ id: 'menu.pages' })}
        </Button>

        <Button variant='text' startIcon={<AccountTreeOutlinedIcon />}
          className={activeButton === 'SERVICES' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => handleMenuButtonClick('SERVICES')}>
          {intl.formatMessage({ id: 'menu.services' })}
        </Button>

        <Button variant='text' startIcon={<InsertLinkOutlinedIcon />}
          className={activeButton === 'LINKS' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => handleMenuButtonClick('LINKS')}>
          {intl.formatMessage({ id: 'menu.links' })}
        </Button>

        <Button variant='text' startIcon={<TranslateOutlinedIcon />}
          className={activeButton === 'LOCALES' ? classes.menuButtonActive : classes.menuButton}
          onClick={() => { }}>
          {intl.formatMessage({ id: 'menu.locales' })}
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
export { Secondary }


