import { Avatar, Box, Breadcrumbs, generateUtilityClass, Link, styled, Typography } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import HomeIcon from '@mui/icons-material/Home';
import HourglassEmptyIcon from '@mui/icons-material/HourglassEmpty';

import { useIntl } from "react-intl";


import { SearchApi } from '../api-search';
import { SiteApi } from '../api-site';
import { GUserOverviewMenuView } from '../g-user-overview-menu';
import { GRouterUnfinishedFormsProps } from "./GRouterUnfinishedForms";

export const MUI_NAME = 'GRouterUnfinishedForms';


export interface GRouterUnfinishedFormsClasses {
  root: string;
  titleLayout: string;
  titleAvatar: string;
  titleText: string;
  subTitleText: string;
}
export type GRouterUnfinishedFormsClassKey = keyof GRouterUnfinishedFormsClasses;


export interface OwnerState {
  viewId: GUserOverviewMenuView;
  topic: SiteApi.TopicView | undefined;

  onTopic: (topic: SiteApi.TopicView) => void;
  onForm: (form: SearchApi.LinkToForm) => void;
  onHome: () => void;
}



export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    titleLayout: ['titleLayout'],
    titleAvatar: ['titleAvatar'],
    titleText: ['titleText'],
    subTitleText: ['subTitleText']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GRouterUnfinishedFormsRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.titleLayout,
      styles.titleAvatar,
      styles.titleText,
      styles.subTitleText
    ];
  },
})<{ ownerState: GRouterUnfinishedFormsProps }>(({ theme, ownerState }) => {
  return {

    '& .GRouterUnfinishedForms-titleLayout': {
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'center'
    },

    '& .GRouterUnfinishedForms-titleAvatar': {
      height: '50px',
      width: '50px',
      alignContent: 'center',
      marginRight: theme.spacing(1),
      backgroundColor: theme.palette.primary.main
    },
    '& .GRouterUnfinishedForms-titleText': {
      ...theme.typography.h1
    },
    '& .GRouterUnfinishedForms-subTitleText': {
      ...theme.typography.body1,
      marginTop: theme.spacing(0.5),
      marginBottom: theme.spacing(0.5)
    },

  }
}
);



export const UnfinishedFormsTitle: React.FC = () => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  return (
    <>
      <Box className={classes.titleLayout}>
        <Avatar className={classes.titleAvatar}>
          <HourglassEmptyIcon fontSize='large' />
        </Avatar>
        <Typography className={classes.titleText}>{intl.formatMessage({ id: 'gamut.forms.unfinished.title' })}</Typography>
      </Box>
      <Typography className={classes.subTitleText}>{intl.formatMessage({ id: 'gamut.forms.unfinished.subtitle' })}</Typography>
    </>
  )
}


export const UnfinishedFormsBreadcrumbs: React.FC<{ onClick: (view: GUserOverviewMenuView | undefined) => void }> = ({ onClick }) => {
  const intl = useIntl();

  return (
    <Breadcrumbs>
      {/* @ts-ignore */} //TODO
      <Link onClick={onClick}>
        <HomeIcon />
        {intl.formatMessage({ id: 'gamut.userOverview.home' })}</Link>
    </Breadcrumbs>)
}
