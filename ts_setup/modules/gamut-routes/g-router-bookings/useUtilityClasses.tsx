import { Avatar, Box, Breadcrumbs, generateUtilityClass, Link, styled, Typography } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { Home as HomeIcon } from '@mui/icons-material';
import { CalendarMonth as CalendarMonthIcon } from '@mui/icons-material';

import { useNavigate } from "@tanstack/react-router";
import { useIntl } from "react-intl";
import { GUserOverviewMenuView } from "@dxs-ts/gamut-primitives";


export const MUI_NAME = 'GRouterBookings';

export interface GRouterBookingsClasses {
  root: string;
  bookingTitleLayout: string;
  bookingTitle: string;
  bookingSubTitle: string;
  bookingBodyText: string;
  bookingBreadcrumbs: string;
  avatar: string;
}

export type GRouterBookingsClassKey = keyof GRouterBookingsClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    bookingTitleLayout: ['bookingTitleLayout'],
    bookingTitle: ['bookingTitle'],
    bookingSubTitle: ['bookingSubTitle'],
    bookingBodyText: ['bookingBodyText'],
    bookingBreadcrumbs: ['bookingBreadcrumbs'],
    avatar: ['avatar']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GRouterBookingsRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.layout,
      styles.bookingTitle,
      styles.bookingTitleLayout,
      styles.bookingSubTitle,
      styles.bookingBodyText,
      styles.bookingBreadcrumbs,
      styles.avatar
    ];
  },
})(({ theme }) => {
  return {

    '.GRouterBookings-bookingTitleLayout': {
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'center'
    },
    '.GRouterBookings-bookingTitle': {
      textAlign: 'center',
      ...theme.typography.h1
    },
    '.GRouterBookings-avatar': {
      height: '50px',
      width: '50px',
      alignContent: 'center',
      marginRight: theme.spacing(1),
      backgroundColor: theme.palette.secondary.main,
    },
    '.GRouterBookings-bookingSubTitle': {
      marginBottom: theme.spacing(1),
      ...theme.typography.h3
    },
    '.GRouterBookings-bookingBodyText': {
      ...theme.typography.body1,
      marginTop: theme.spacing(0.5),
      marginBottom: theme.spacing(0.5)
    },
    '.GRouterBookings-bookingBreadcrumbs': {

    }

  }
});



export const BookingsTitle: React.FC = () => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  return (
    <>
      <Box className={classes.bookingTitleLayout}>
        <Avatar className={classes.avatar}>
          <CalendarMonthIcon fontSize='large' />
        </Avatar>
        <Typography className={classes.bookingTitle}>{intl.formatMessage({ id: 'gamut.bookings.title' })}</Typography>
      </Box>
      <Typography className={classes.bookingBodyText}>{intl.formatMessage({ id: 'gamut.bookings.subtitle' })}</Typography>
    </>
  )
}



export const BookingsBreadcrumbs: React.FC = () => {
  const intl = useIntl();
  const nav = useNavigate();
  const classes = useUtilityClasses();

  function handleNav(viewId: GUserOverviewMenuView | undefined) {
    if (!viewId) {
      return;
    }
    nav({
      from: '/secured/$locale/views/$viewId',
      params: { viewId },
      to: '/secured/$locale/views/$viewId',
    })
  }
  return (
    <Breadcrumbs className={classes.bookingBreadcrumbs}>
      <Link onClick={() => handleNav('user-overview')}>
        <HomeIcon />
        {intl.formatMessage({ id: 'gamut.userOverview.home' })}</Link>
    </Breadcrumbs>
  )
}



