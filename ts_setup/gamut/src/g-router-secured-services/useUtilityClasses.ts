import { Breadcrumbs, generateUtilityClass, styled } from "@mui/material";
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
  servicesBreadcrumbs: string

}
export type GRouterSecuredServicesClassKey = keyof GRouterSecuredServicesClasses;


export interface OwnerState {
  viewId: GUserOverviewMenuView;
  topic: SiteApi.TopicView | undefined;
  withDrawer: boolean;
  search: SearchApi.SearchState;
  setSearch: React.Dispatch<React.SetStateAction<SearchApi.SearchState>>;
  onTopic: (topic: SiteApi.TopicView) => void;
  onForm: (form: SearchApi.LinkToForm) => void;
  onHome: () => void;
}



export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    searchFilterButtons: ['searchFilterButtons'],
    searchResults: ['searchResults'],
    resultsDividerTitle: ['resultsDividerTitle'],
    resultsDivider: ['resultsDivider'],
    servicesBreadcrumbs: ['servicesBreadcrumbs']
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
    '& .GRouterSecuredServices-resultsDividerTitle': {

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
    '& .GRouterSecuredServices-servicesBreadcrumbs': {

    }

  }
});

export const GRouterSecuredServicesFilterButtonsRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },
})(({ theme }) => {
  return {


  }
}
);

export const GRouterSecuredServicesSearchResultsRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
    ];
  },
})(({ theme }) => {
  return {

  }
}
);

export const GRouterSecuredServicesResultsDividerRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
    ];
  },
})(({ theme }) => {
  return {


  }
});

export const GRouterSecuredServicesBreadcrumbsRoot = styled(Breadcrumbs, {
  name: MUI_NAME,
  slot: 'ServicesBreadcrumbs',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
    ];
  },
})(({ theme }) => {
  return {

  }
});
