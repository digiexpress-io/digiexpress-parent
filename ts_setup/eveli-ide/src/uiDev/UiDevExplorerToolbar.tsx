import React from 'react';
import { IconButton, Typography } from '@mui/material';
import MenuOutlinedIcon from '@mui/icons-material/MenuOutlined';
import DashboardCustomizeOutlinedIcon from '@mui/icons-material/DashboardCustomizeOutlined';
import SaveOutlinedIcon from '@mui/icons-material/SaveOutlined';
import HelpOutlineOutlinedIcon from '@mui/icons-material/HelpOutlineOutlined';
import LanguageIcon from '@mui/icons-material/Language';
import SearchIcon from '@mui/icons-material/Search';


import { LocaleSelect } from './LocaleSelect';
import { useUtilityClasses } from './useUtilityClasses';


export const UiDevExplorerToolbar: React.FC = () => {
  const classes = useUtilityClasses();
  const [anchorEl, setAnchorEl] = React.useState<HTMLButtonElement | null>(null);

  const handleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };


  return (<>
    <LocaleSelect open={!!anchorEl} onClose={handleClose} anchorEl={anchorEl} />
    <div className={classes.explorerToolbar}>
      <div style={{ textAlign: 'center' }}>
        <IconButton className={classes.toolbarIcon}><MenuOutlinedIcon /></IconButton>
      </div>

      <div style={{ textAlign: 'center' }}>
        <IconButton className={classes.toolbarIcon}><DashboardCustomizeOutlinedIcon /></IconButton>
        <Typography className={classes.toolbarIconText}>Activities</Typography>
      </div>

      <div style={{ textAlign: 'center' }}>
        <IconButton className={classes.toolbarIcon}><SaveOutlinedIcon /></IconButton>
        <Typography className={classes.toolbarIconText}>Save</Typography>
      </div>

      <div style={{ textAlign: 'center' }}>
        <IconButton className={classes.toolbarIcon}><SearchIcon /></IconButton>
        <Typography className={classes.toolbarIconText}>Search</Typography>
      </div>

      <div style={{ textAlign: 'center' }}>
        <IconButton className={classes.toolbarIcon}><HelpOutlineOutlinedIcon /></IconButton>
        <Typography className={classes.toolbarIconText}>Help</Typography>
      </div>

      <div style={{ textAlign: 'center' }}>
        <IconButton className={classes.toolbarIcon} onClick={handleClick}><LanguageIcon /></IconButton>
        <Typography className={classes.toolbarIconText}>EN</Typography>
      </div>
    </div>
  </>

  )
}