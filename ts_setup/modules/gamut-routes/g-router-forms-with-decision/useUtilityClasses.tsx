import { Avatar, Box, Breadcrumbs, generateUtilityClass, Link, styled, Typography } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { Home as HomeIcon } from '@mui/icons-material';
import { HourglassBottom as HourglassBottomIcon } from '@mui/icons-material';

import { useNavigate } from "@tanstack/react-router";
import { useIntl } from "react-intl";
import { GUserOverviewMenuView } from "@dxs-ts/gamut-primitives";


export const MUI_NAME = 'GRouterFormsWithDecision';

export interface GRouterFormsWithDecisionClasses {
  root: string;
  withDecisionTitle: string;
  withDecisionTitleLayout: string;
  withDecisionBodyText: string;
  withDecisionBreadcrumbs: string;
  avatar: string;
}

export type GRouterFormsWithDecisionClassKey = keyof GRouterFormsWithDecisionClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    withDecisionTitle: ['withDecisionTitle'],
    withDecisionTitleLayout: ['withDecisionTitleLayout'],
    withDecisionBodyText: ['withDecisionBodyText'],
    withDecisionBreadcrumbs: ['withDecisionBreadcrumbs'],
    avatar: ['avatar']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GRouterFormsWithDecisionRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.withDecisionTitle,
      styles.withDecisionTitleLayout,
      styles.withDecisionBodyText,
      styles.withDecisionBreadcrumbs,
      styles.avatar
    ];
  },
})(({ theme }) => {
  return {
    '.GRouterFormsWithDecision-withDecisionTitleLayout': {
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'center'
    },
    '.GRouterFormsWithDecision-withDecisionTitle': {
      textAlign: 'center',
      ...theme.typography.h1
    },
    '.GRouterFormsWithDecision-avatar': {
      height: '50px',
      width: '50px',
      alignContent: 'center',
      marginRight: theme.spacing(1),
      backgroundColor: theme.palette.success.main,
    },
    '.GRouterFormsWithDecision-withDecisionBodyText': {
      ...theme.typography.body1,
      marginTop: theme.spacing(0.5),
      marginBottom: theme.spacing(0.5)
    },
    '.GRouterFormsWithDecision-withDecisionBreadcrumbs': {

    }

  }
});



export const WithDecisionTitle: React.FC = () => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  return (
    <>
      <Box className={classes.withDecisionTitleLayout}>
        <Avatar className={classes.avatar}>
          <HourglassBottomIcon fontSize='large' />
        </Avatar>
        <Typography className={classes.withDecisionTitle}>{intl.formatMessage({ id: 'gamut.forms.withDecision.title' })}</Typography>
      </Box>
      <Typography className={classes.withDecisionBodyText}>{intl.formatMessage({ id: 'gamut.forms.withDecision.subtitle' })}</Typography>
    </>
  )
}



export const WithDecisionBreadcrumbs: React.FC = () => {
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
    <Breadcrumbs className={classes.withDecisionBreadcrumbs}>
      <Link onClick={() => handleNav('user-overview')}>
        <HomeIcon />
        {intl.formatMessage({ id: 'gamut.userOverview.home' })}
      </Link>
    </Breadcrumbs>
  )
}



