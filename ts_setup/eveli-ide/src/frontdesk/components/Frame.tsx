import { AppBar, CssBaseline, Toolbar, Typography, Button, Menu, MenuItem, Box, useTheme } from '@mui/material';
import React, { useContext, useState } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import LanguageIcon from '@mui/icons-material/Translate';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ExitToAppIcon from '@mui/icons-material/ExitToApp';
import VpnKeyIcon from '@mui/icons-material/VpnKey';
import FeedbackOutlinedIcon from '@mui/icons-material/FeedbackOutlined';
import { FormattedMessage, useIntl } from 'react-intl';
import { AppMenu } from './AppMenu';
import { Impersonation } from './Impersonation';
import { useUserInfo } from '../context/UserContext';
import { Feedback } from './Feedback';
import { FeedbackContext } from '../context/FeedbackContext';
import { FEEDBACK_ROLES } from '../util/rolemapper';

import IconButton from '@mui/material/IconButton';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import { styled, Theme, CSSObject } from '@mui/material/styles';
import MuiDrawer from '@mui/material/Drawer';

const drawerWidth = 240;
const UI_LANGUAGES = ['en', 'fi', 'sv'];

const HOST_URL = (window as any).env.VITE_HOST_URL || 'http://localhost:3000';
const ENV_TYPE: 'prod' | 'test' = (window as any).env.VITE_ENV_TYPE || 'test';


const toolbarItemStyle = {
  ml: 1,
};

const languageLabelStyle = {
  display: {
    xs: 'none',
    md: 'block'
  }
};

const classes = {
  root: {
    display: 'flex'
  },
  rootUnauth: {
    display: 'flex',
    flexDirection: 'column',
    minHeight: '100vh'
  },
  drawer: {
    width: drawerWidth,
    flexShrink: 0,
  },
  drawerPaper: {
    width: drawerWidth,
  },
  spacer: {
    flexGrow: 1
  },
  toolbarImage: {
    verticalAlign: 'middle',
    margin: '2px'
  },
  title: {
    flexGrow: 1,
  },
};

const openedMixin = (theme: Theme): CSSObject => ({
  width: drawerWidth,
  transition: theme.transitions.create('width', {
    easing: theme.transitions.easing.sharp,
    duration: theme.transitions.duration.enteringScreen,
  }),
  overflowX: 'hidden',
});

const closedMixin = (theme: Theme): CSSObject => ({
  transition: theme.transitions.create('width', {
    easing: theme.transitions.easing.sharp,
    duration: theme.transitions.duration.leavingScreen,
  }),
  overflowX: 'hidden',
  width: `calc(${theme.spacing(7)} + 1px)`,
  [theme.breakpoints.up('sm')]: {
    width: `calc(${theme.spacing(8)} + 1px)`,
  },
});

export interface FrameProps {
  setLocale: (locale: string) => void;
}

const Drawer = styled(MuiDrawer, { shouldForwardProp: (prop) => prop !== 'open' })(
  ({ theme, open }) => ({
    width: drawerWidth,
    flexShrink: 0,
    whiteSpace: 'nowrap',
    boxSizing: 'border-box',
    ...(open && {
      ...openedMixin(theme),
      '& .MuiDrawer-paper': openedMixin(theme),
    }),
    ...(!open && {
      ...closedMixin(theme),
      '& .MuiDrawer-paper': closedMixin(theme),
    }),
  }),
);


export const Frame: React.FC<FrameProps> = ({ setLocale }) => {
  const navigate = useNavigate();
  const intl = useIntl();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const userInfo = useUserInfo();
  const context = useContext(FeedbackContext);

  const theme = useTheme();
  const toolbarStyle = theme.mixins.toolbar;

  const handleLanguageMenuOpen = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  }

  const handleLanguageMenuClose = () => {
    setAnchorEl(null);
  }

  const handleLanguageSelect = (language: string) => {
    handleLanguageMenuClose();
    setLocale(language);
  }

  const showSideBar = userInfo.isAuthenticated() && userInfo.isAuthorized();


  const [isOpen, setOpen] = React.useState(true);
  const handleMuiDrawerClick = () => {
    setOpen(!isOpen);
  }

  return (
    <div style={userInfo.isAuthenticated() ? classes.root : classes.rootUnauth}>
      <CssBaseline />
      <AppBar position='fixed' sx={{ bgcolor: 'background.paper', zIndex: (theme) => theme.zIndex.drawer + 1 }} elevation={1} >
        <Toolbar>
          <Typography variant='h4' color='inherit' noWrap>
            <span onClick={() => navigate('/')}>
              <div style={classes.toolbarImage}>LOGO</div>
            </span>
          </Typography>

          <Box sx={classes.spacer} />
          {userInfo.isAuthenticated() &&
            <Impersonation />
          }
          {
            userInfo.isAuthenticated() && (ENV_TYPE !== 'prod' || userInfo.hasRole(...FEEDBACK_ROLES)) &&
            <Button startIcon={<FeedbackOutlinedIcon />}
              sx={toolbarItemStyle}
              variant='contained'
              onClick={() => context.open()}>
              <FormattedMessage id={'app.feedback'} />
            </Button>
          }
          <Button aria-controls='language-menu' variant='contained' aria-haspopup='true' onClick={handleLanguageMenuOpen} sx={toolbarItemStyle} >
            <LanguageIcon />
            <Box sx={languageLabelStyle}><FormattedMessage id={`locale.${intl.locale}`} /></Box>
            <ExpandMoreIcon fontSize="small" />
          </Button>
          <Menu id='language-menu' anchorEl={anchorEl} keepMounted open={Boolean(anchorEl)} onClose={handleLanguageMenuClose}>
            {
              UI_LANGUAGES.map(lang => <MenuItem key={lang} selected={lang === intl.locale} onClick={() => handleLanguageSelect(lang)}><FormattedMessage id={`locale.${lang}`} /></MenuItem>)
            }
          </Menu>

          {
            userInfo.isAuthenticated() ?
              <Button startIcon={<ExitToAppIcon />}
                sx={toolbarItemStyle}
                variant='contained'
                onClick={() => window.location.href = `${HOST_URL}/logout`}>
                <FormattedMessage id={'app.logout'} />
              </Button>
              :
              <Button startIcon={<VpnKeyIcon />}
                sx={toolbarItemStyle}
                variant='contained'
                onClick={() => window.location.href = `${HOST_URL}/oauth2/authorization/oidcprovider`}>
                <FormattedMessage id={'app.login'} />
              </Button>
          }
        </Toolbar>
      </AppBar>
      {
        showSideBar &&
        <Drawer
          sx={classes.drawer}
          variant='permanent'
          anchor='left'
          open={isOpen}
        >
          <Box sx={toolbarStyle} />
          <div />
          <AppMenu open={isOpen} />
          <IconButton onClick={handleMuiDrawerClick} >
            {isOpen ? <ChevronLeftIcon /> : <ChevronRightIcon />}
          </IconButton>
        </Drawer>
      }
      <main style={{ width: '100%' }}>
        <Box
          sx={{
            fG: 1,
            bgcolor: 'theme.palette.default',
            p: 3
          }}>
          <Box
            sx={toolbarStyle} />
          <div />
          <Outlet />
        </Box>
      </main>
      <Feedback />
    </div>
  );
}
