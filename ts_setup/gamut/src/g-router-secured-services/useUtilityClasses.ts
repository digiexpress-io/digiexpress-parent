import { generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { SearchApi } from '../api-search';
import { SiteApi } from '../api-site';
import { GUserOverviewMenuView } from '../g-user-overview-menu';

export const MUI_NAME = 'GRouterSecuredServices';


export interface GRouterSecuredServicesClasses {
  root: string;
  searchFilterButtons: string,
  searchResults: string,
  resultsDividerTitle: string,
  resultsDivider: string,
  servicesBreadcrumbs: string,
  childTopic: string;
}
export type GRouterSecuredServicesClassKey = keyof GRouterSecuredServicesClasses;


export interface OwnerState {
  locale: string;
  defaultViewId: string;
  viewId: GUserOverviewMenuView;
  topic: SiteApi.TopicView | undefined;
  withDrawer: boolean;
  onTopic: (topic: SiteApi.TopicView) => void;
  onForm: (form: SearchApi.LinkToForm) => void;
}



export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    searchFilterButtons: ['searchFilterButtons'],
    searchResults: ['searchResults'],
    resultsDividerTitle: ['resultsDividerTitle'],
    resultsDivider: ['resultsDivider'],
    servicesBreadcrumbs: ['servicesBreadcrumbs'],
    childTopic: ['childTopic']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GRouterSecuredServicesRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.searchResults,
      styles.searchFilterButtons,
      styles.resultsDividerTitle,
      styles.resultsDivider,
      styles.servicesBreadcrumbs
    ];
  },
})(({ theme }) => {
  return {

    '& .GRouterSecuredServices-resultsDividerTitle': {
      paddingBotton: theme.spacing(1),

      [theme.breakpoints.down('md')]: {
        ...theme.typography.h1,
        textAlign: 'center',
      },

      [theme.breakpoints.up('md')]: {
        ...theme.typography.h3,
        textAlign: 'center',
      },
    },
    '& .GRouterSecuredServices-resultsDivider': {
      border: `1px solid ${theme.palette.primary.main}`,
      marginTop: theme.spacing(1),
      marginBottom: theme.spacing(1)
    },
    '& .GRouterSecuredServices-searchResults': {
      gap: theme.spacing(1),
      padding: theme.spacing(1),
    },
    '& .GRouterSecuredServices-searchFilterButtons': {
      display: 'flex',
      flexDirection: 'column',
      gap: theme.spacing(1),
      padding: theme.spacing(1),
    },
    '& .GRouterSecuredServices-childTopic': {
      display: 'flex',
      alignItems: 'center',
      ...theme.typography.body2,
      color: theme.palette.text.secondary,
      marginLeft: theme.spacing(1),
      marginTop: 0,
      marginBottom: 0,
    },
    
    '& .GRouterSecuredServices-childTopic .MuiSvgIcon-root': {
      fontSize: '6pt',
      marginRight: theme.spacing(1),
      color: theme.palette.primary.main,
    },
  }
});


