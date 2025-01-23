import { Avatar, Box, Breadcrumbs, generateUtilityClass, Link, styled, Typography } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import HomeIcon from '@mui/icons-material/Home';
import HourglassTopIcon from '@mui/icons-material/HourglassTop';

import { useNavigate } from "@tanstack/react-router";
import { useIntl } from "react-intl";
import { GUserOverviewMenuView } from "../g-user-overview-menu";


export const MUI_NAME = 'GFormsAwaitingDecision';

export interface GFormsAwaitingDecisionClasses {
  root: string;
  awaitingDecisionTitle: string;
  awaitingDecisionTitleLayout: string;
  awaitingDecisionSubTitle: string;
  awaitingDecisionBodyText: string;
  awaitingDecisionBreadcrumbs: string;
  avatar: string;
}

export type GFormsAwaitingDecisionClassKey = keyof GFormsAwaitingDecisionClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    awaitingDecisionTitle: ['awaitingDecisionTitle'],
    awaitingDecisionTitleLayout: ['awaitingDecisionTitleLayout'],
    awaitingDecisionSubTitle: ['awaitingDecisionSubTitle'],
    awaitingDecisionBodyText: ['awaitingDecisionBodyText'],
    awaitingDecisionBreadcrumbs: ['awaitingDecisionBreadcrumbs'],
    avatar: ['avatar']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GFormsAwaitingDecisionRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.awaitingDecisionTitle,
      styles.awaitingDecisionTitleLayout,
      styles.awaitingDecisionSubTitle,
      styles.awaitingDecisionBodyText,
      styles.awaitingDecisionBreadcrumbs,
      styles.avatar
    ];
  },
})(({ theme }) => {
  return {
    '.GFormsAwaitingDecision-awaitingDecisionTitleLayout': {
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'center'
    },
    '.GFormsAwaitingDecision-awaitingDecisionTitle': {
      textAlign: 'center',
      ...theme.typography.h1
    },
    '.GFormsAwaitingDecision-avatar': {
      height: '50px',
      width: '50px',
      alignContent: 'center',
      marginRight: theme.spacing(1),
      backgroundColor: theme.palette.warning.main,
    },
    '.GFormsAwaitingDecision-awaitingDecisionSubTitle': {
      marginBottom: theme.spacing(1),
      ...theme.typography.h3
    },
    '.GFormsAwaitingDecision-awaitingDecisionBodyText': {
      ...theme.typography.body1,
      marginTop: theme.spacing(0.5),
      marginBottom: theme.spacing(0.5)
    },
    '.GFormsAwaitingDecision-awaitingDecisionBreadcrumbs': {

    }

  }
});



export const AwaitingDecisionTitle: React.FC = () => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  return (
    <>
      <Box className={classes.awaitingDecisionTitleLayout}>
        <Avatar className={classes.avatar}>
          <HourglassTopIcon fontSize='large' />
        </Avatar>
        <Typography className={classes.awaitingDecisionTitle}>{intl.formatMessage({ id: 'gamut.forms.awaitingDecision.title' })}</Typography>
      </Box>
      <Typography className={classes.awaitingDecisionBodyText}>{intl.formatMessage({ id: 'gamut.forms.awaitingDecision.subtitle' })}</Typography>
    </>
  )
}



export const AwaitingDecisionsBreadcrumbs: React.FC = () => {
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
    <Breadcrumbs className={classes.awaitingDecisionBreadcrumbs}>
      <Link onClick={() => handleNav('user-overview')}>
        <HomeIcon />
        {intl.formatMessage({ id: 'gamut.userOverview.home' })}
      </Link>
    </Breadcrumbs>
  )
}



