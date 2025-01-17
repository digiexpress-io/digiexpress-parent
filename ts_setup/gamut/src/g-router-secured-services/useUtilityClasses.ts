import { Breadcrumbs, generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { SearchApi } from '../api-search';
import { SiteApi } from '../api-site';
import { GUserOverviewMenuView } from '../g-user-overview-menu';

export const MUI_NAME = 'GRouterSecuredServices';


export interface GRouterSecuredServicesClasses {
  root: string;
  searchFilterButtons: string,
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
    searchFilterButtons: ['searchFilterButtons']
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
    ];
  },
})(({ theme }) => {
  return {


  }
});

export const GRouterSecuredServicesFilterButtonsRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.searchFilterButtons
    ];
  },
})(({ theme }) => {
  return {
    gap: theme.spacing(1),
    padding: theme.spacing(1),
  }
}
);


export const GRouterSecuredServicesBreadcrumbsRoot = styled(Breadcrumbs, {
  name: MUI_NAME,
  slot: 'BreadcrumbsRoot',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
    ];
  },
})(({ theme }) => {
  return {


  }
});
