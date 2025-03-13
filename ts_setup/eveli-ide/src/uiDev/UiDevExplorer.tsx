import React from 'react';
import { useUtilityClasses } from './useUtilityClasses';
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

import logo from './logoLifeDigitalDark.svg';
import { UiDevExplorerToolbar } from './UiDevExplorerToolbar';
import { ComposeSelect } from './ComposeSelect';
import { AssetType } from './types';



export const UiDevExplorer: React.FC = () => {
  const classes = useUtilityClasses();
  const [activeButton, setActiveButton] = React.useState<AssetType>('ARTICLES');
  const [anchorEl, setAnchorEl] = React.useState<HTMLButtonElement | null>(null);


  const handleComposeSelectClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleComposeSelectClose = () => {
    setAnchorEl(null);
  };


  function handleButtonClick(buttonId: AssetType) {
    setActiveButton(prev => (prev === buttonId ? undefined : buttonId));
  }



  return (<>
    <ComposeSelect open={!!anchorEl} anchorEl={anchorEl} onClose={handleComposeSelectClose} />
    <UiDevExplorerToolbar />

    <div className={classes.explorerContainer}>

      <div className={classes.logoContainer}>
        <img src={logo} className={classes.logo} />
      </div>

      <Button startIcon={<CreateOutlinedIcon />} className={classes.composeButton} onClick={handleComposeSelectClick}>Compose</Button>

      <Button variant='text' startIcon={<MenuBookOutlinedIcon />}
        className={activeButton === 'ARTICLES' ? classes.menuButtonActive : classes.menuButton}
        onClick={() => handleButtonClick('ARTICLES')}>
        Articles
      </Button>

      <Button variant='text' startIcon={<DescriptionOutlinedIcon />}
        className={activeButton === 'PAGES' ? classes.menuButtonActive : classes.menuButton}
        onClick={() => handleButtonClick('PAGES')}>
        Pages
      </Button>

      <Button variant='text' startIcon={<AccountTreeOutlinedIcon />}
        className={activeButton === 'SERVICES' ? classes.menuButtonActive : classes.menuButton}
        onClick={() => handleButtonClick('SERVICES')}>
        Services
      </Button>

      <Button variant='text' startIcon={<InsertLinkOutlinedIcon />}
        className={activeButton === 'LINKS' ? classes.menuButtonActive : classes.menuButton}
        onClick={() => handleButtonClick('LINKS')}>
        Links
      </Button>

      <Divider className={classes.explorerDivider} />

      <Button variant='text' startIcon={<TranslateOutlinedIcon />}
        className={activeButton === 'LOCALES' ? classes.menuButtonActive : classes.menuButton}
        onClick={() => handleButtonClick('LOCALES')}>
        Locales
      </Button>

      <Button variant='text' startIcon={<UploadFileOutlinedIcon />}
        className={activeButton === 'MIGRATIONS' ? classes.menuButtonActive : classes.menuButton}
        onClick={() => handleButtonClick('MIGRATIONS')}>
        Migrations
      </Button>

      <Button variant='text' startIcon={<FormatShapesOutlinedIcon />}
        className={activeButton === 'TEMPLATES' ? classes.menuButtonActive : classes.menuButton}
        onClick={() => handleButtonClick('TEMPLATES')}>
        Templates
      </Button>

      <Divider className={classes.explorerDivider} />

      <Button className={classes.logoutButton}
        variant="text"
        startIcon={<LogoutIcon />}
        onClick={() => console.log("log out")}
      >
        <Stack spacing={0} alignItems="flex-start">
          <Typography variant="body2">Log out</Typography>
          <Typography variant="caption">John Jacob Smith</Typography>
        </Stack>
      </Button>

    </div>
  </>
  )
}