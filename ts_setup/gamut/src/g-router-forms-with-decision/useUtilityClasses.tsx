import { Avatar, Box, Breadcrumbs, generateUtilityClass, Link, styled, Typography } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import HomeIcon from '@mui/icons-material/Home';
import HourglassBottomIcon from '@mui/icons-material/HourglassBottom';

import { useNavigate } from "@tanstack/react-router";
import { useIntl } from "react-intl";
import { GUserOverviewMenuView } from "../g-user-overview-menu";


export const MUI_NAME = 'GFormsWithDecision';

export interface GFormsWithDecisionClasses {
  root: string;
  withDecisionTitle: string;
  withDecisionTitleLayout: string;
  withDecisionBodyText: string;
  withDecisionBreadcrumbs: string;
  avatar: string;
}

export type GFormsWithDecisionClassKey = keyof GFormsWithDecisionClasses;

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


export const GFormsWithDecisionRoot = styled("div", {
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
    '.GFormsWithDecision-withDecisionTitleLayout': {
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'center'
    },
    '.GFormsWithDecision-withDecisionTitle': {
      textAlign: 'center',
      ...theme.typography.h1
    },
    '.GFormsWithDecision-avatar': {
      height: '50px',
      width: '50px',
      alignContent: 'center',
      marginRight: theme.spacing(1),
      backgroundColor: theme.palette.success.main,
    },
    '.GFormsWithDecision-withDecisionBodyText': {
      ...theme.typography.body1,
      marginTop: theme.spacing(0.5),
      marginBottom: theme.spacing(0.5)
    },
    '.GFormsWithDecision-withDecisionBreadcrumbs': {

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



