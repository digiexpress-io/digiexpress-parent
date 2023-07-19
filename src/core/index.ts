import { AppProviderProps, AppProvider } from './context/AppContext';
import { useDrawer as useDrawerAlias } from './context/drawer/DrawerContext';
import { useTabs as useTabsAlias } from './context/tabs/TabsContext';
import { useSecondary as useSecondaryAlias } from './context/secondary/SecondaryContext';
import { siteTheme } from './theme/siteTheme';
import { PortalApp } from './app/PortalApp';
import { ServiceConfig, Service, TopicHeading, TopicId, TopicLinkId, TopicLink, TopicView, Topic, Blob, Site, TopicLinkType, LocaleCode, FallbackSites, createService as createServiceAs } from './service';
import intlMessages from './intl';
import PortalStyles from './styles';

import {
  App, AppId, MediaQuery, StyleQuery, BreakpointConfig, ToolbarProps, PrimaryProps, SecondaryProps
} from './context/AppAPI';

import {
  DrawerContextType, DrawerSession, DrawerActions
} from './context/drawer/DrawerAPI';
import {
  DrawerProvider as DrawerProviderAs
} from './context/drawer/DrawerContext';

import {
  BreakpointProvider as BreakpointProviderAs,
  useBreakpoint as useBreakpointAs,
  BreakpointMode, BreakpointContextType
} from './context/breakpoint/BreakpointContext';


import {
  TabsContextType, TabsSession, TabSession, TabsHistory, TabsActions
} from './context/tabs/TabsAPI';
import {
  TabsProvider as TabsProviderAs
} from './context/tabs/TabsContext';


import {
  SecondaryContextType, SecondarySession, SecondaryActions 
} from './context/secondary/SecondaryAPI';

import {
  SecondaryProvider as SecondaryProviderAs
} from './context/secondary/SecondaryContext';


import {
  SiteProvider as SiteProviderAs, SiteProviderProps
} from './context/site/Context';
import {
  SiteContextType, SiteActionOverrides
} from './context/site/ContextTypes';
import {
  SiteState
} from './context/site/contextReducer';
import {
  useContext as useSiteContext
} from './context/site/useContext';



import {
  TopicsProvider as TopicsProviderAs, TopicsProviderProps
} from './context/topics/Context';
import {
  TopicsContextType, TopicsActionOverrides
} from './context/topics/ContextTypes';
import {
  TopicsState
} from './context/topics/contextReducer';
import {
  useContext as useTopicsContext, 
  useTopic as useTopicContext, 
  useBlob as useBlobContext
} from './context/topics/useContext';


// import { StyledDialog, StyledDialogProps } from './styles/StyledDialog';


declare namespace Portal  { //ONLY can export interfaces and types with 'declare namespace'. DOES NOT COMPILE with constants
  export { 
    TopicId, TopicLinkId,
    AppProviderProps, App, AppId, MediaQuery, StyleQuery, BreakpointConfig,
    DrawerContextType, DrawerSession, DrawerActions,
    TabsContextType, TabsSession, TabSession, TabsHistory, TabsActions,
    SecondaryContextType, SecondarySession, SecondaryActions,
    SiteContextType, SiteState, SiteProviderProps, SiteActionOverrides,
    BreakpointMode, BreakpointContextType,
    LocaleCode, FallbackSites, 
    ServiceConfig, Service, TopicHeading, TopicLink, Topic, TopicView, Blob, Site, TopicLinkType,
    ToolbarProps, PrimaryProps, SecondaryProps,
    TopicsProviderProps, TopicsActionOverrides, TopicsContextType, TopicsState, PortalStyles
  };
}

namespace Portal { //export the constants
  export const SiteProvider = SiteProviderAs;
  export const DrawerProvider = DrawerProviderAs;
  export const TabsProvider = TabsProviderAs;
  export const SecondaryProvider = SecondaryProviderAs;
  export const BreakpointProvider = BreakpointProviderAs;
  export const TopicsProvider = TopicsProviderAs;
  export const Provider = AppProvider;
  
  
  export const useBreakpoint = useBreakpointAs; 
  export const useDrawer = useDrawerAlias;
  export const useTabs = useTabsAlias;
  export const useSecondary = useSecondaryAlias;
  export const useTopics = useTopicsContext;
  export const useSite = useSiteContext;
  export const useTopic = useTopicContext;
  export const useBlob = useBlobContext;  
  
  export const DefaultApp = PortalApp;
  export const defaultTheme = siteTheme;
  export const messages = intlMessages;
  export const createService = createServiceAs;
  export const Styles = PortalStyles;
}

export default Portal;