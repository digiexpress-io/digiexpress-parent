import { Divider, generateUtilityClass, styled, Typography } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { useIntl } from "react-intl";


import { SearchApi } from '../api-search';
import { SiteApi } from '../api-site';
import { GUserOverviewMenuView } from '../g-user-overview-menu';

export const MUI_NAME = 'GRouterUserOverview';


export interface GRouterUserOverviewClasses {
  root: string;
  titleLayout: string;
  titleText: string;
  subTitleText: string;
}
export type GRouterUserOverviewClassKey = keyof GRouterUserOverviewClasses;


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
    titleText: ['titleText'],
    subTitleText: ['subTitleText']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GRouterUserOverviewRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.titleLayout,
      styles.titleText,
      styles.subTitleText
    ];
  },
})(({ theme }) => {
  return {

    '& .GRouterUserOverview-titleLayout': {
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'center'
    },
    '& .GRouterUserOverview-titleText': {
      ...theme.typography.h1
    },
    '& .GRouterUserOverview-subTitleText': {
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
      <Typography className={classes.titleText}>{intl.formatMessage({ id: 'gamut.userOverview.welcome.title' })}</Typography>
      <Typography className={classes.subTitleText}>{intl.formatMessage({ id: 'gamut.userOverview.welcome.desc' })}</Typography>
      <Divider />
    </>
  )
}


