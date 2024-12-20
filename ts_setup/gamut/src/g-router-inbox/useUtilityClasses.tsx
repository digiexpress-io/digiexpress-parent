import React from 'react';
import { Avatar, Box, Breadcrumbs, generateUtilityClass, Link, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import MailOutlineIcon from '@mui/icons-material/MailOutline';
import HomeIcon from '@mui/icons-material/Home';

import { useNavigate } from '@tanstack/react-router';
import { useIntl } from 'react-intl';

import { GUserOverviewMenuView } from '../g-user-overview-menu';


export const MUI_NAME = 'GRouterInbox';

export interface GRouterInboxClasses {
  root: string,
  title: string,
  subTitle: string,
  topTitle: string,
  topTitleIcon: string,
  topTitleLayout: string
}
export type GRouterInboxClassKey = keyof GRouterInboxClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    subTitle: ['subTitle'],
    topTitle: ['topTitle'],
    topTitleIcon: ['topTitleIcon'],
    topTitleLayout: ['topTitleLayout']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GRouterInboxRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.title,
      styles.subTitle,
      styles.topTitle,
      styles.topTitleIcon,
      styles.topTitleLayout
    ];
  },
})(({ theme }) => {
  return {
    '.GRouterInbox-topTitleLayout': {
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'center'
    },
    '.GRouterInbox-topTitle': {
      height: '50px',
      width: '50px',
      alignContent: 'center',
      marginRight: theme.spacing(1),
      backgroundColor: theme.palette.primary.main,
    },
    '.GRouterInbox-topTitleIcon': {
      fontSize: '20pt',
    },
    '.GRouterInbox-title': {
      ...theme.typography.h1
    },
    '.GRouterInbox-subTitle': {
      variant: 'body1',
      marginLeft: theme.spacing(1),
      marginTop: theme.spacing(1)
    }
  };
});



export const Bread: React.FC = () => {
  const intl = useIntl();
  const nav = useNavigate();

  function handleNav(viewId: GUserOverviewMenuView | undefined) {
    if (!viewId) { // i.e. --> login/logout buttons
      return;
    }
    nav({
      from: '/secured/$locale/views/$viewId',
      params: { viewId },
      to: '/secured/$locale/views/$viewId',
    })
  }
  return (
    <Breadcrumbs>
      <Link onClick={() => handleNav('user-overview')}>
        <HomeIcon />
        {intl.formatMessage({ id: 'gamut.userOverview.home' })}
      </Link>
      <Typography>{intl.formatMessage({ id: 'gamut.inbox.title' })}</Typography>
    </Breadcrumbs>
  )
}


export const Top: React.FC = () => {
  const classes = useUtilityClasses();
  const intl = useIntl();

  return (
    <>
      <Box className={classes.topTitleLayout}>
        <Avatar className={classes.topTitle}>
          <MailOutlineIcon className={classes.topTitleIcon} />
        </Avatar>
        <Typography className={classes.title}>{intl.formatMessage({ id: 'gamut.inbox.title' })}</Typography>
      </Box>
      <Typography className={classes.subTitle}>{intl.formatMessage({ id: 'gamut.inbox.subtitle' })}</Typography>
    </>);
}
