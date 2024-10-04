import React from 'react';
import { IntlProvider } from 'react-intl'
import { SxProps } from '@mui/system';

import Portal from '../'
import { Toolbar } from './toolbar';
import { Primary } from './primary/Primary';
import { Secondary } from './secondary/Secondary';
import { UI_LANGUAGES, DEFAULT_LOCALE } from './util';


const scroll: SxProps = {
  overflowY: "auto",
  '&::-webkit-scrollbar': {
    width: '0.7em',
  },
  '&::-webkit-scrollbar-track': {
    borderRadius: '15px',
    backgroundColor: 'primary.light'
  },
  '&::-webkit-scrollbar-thumb': {
    borderRadius: '15px',
    backgroundColor: 'primary.main',
  },
}

const app: Portal.App = {
  id: "demoApp",
  components: (mode: Portal.BreakpointMode) => ({ primary: Primary, secondary: Secondary, toolbar: Toolbar }),
  config: {
    mobile: {
      breakpoint: (theme, query) => query(theme.breakpoints.down("sm")),
      drawerWidth: (theme, query) => "100%",
      toolbarHeight: 100,
      main: ({ drawerOpen }) => drawerOpen ? { display: "none" } : scroll,
      secondary: ({ drawerOpen }) => scroll,
      drawerOpen: false
    },
    tablet: {
      breakpoint: (theme, query) => query(theme.breakpoints.down("md")),
      drawerWidth: (theme, query) => 400,
      toolbarHeight: 125,
      main: ({ drawerOpen }) => scroll,
      secondary: ({ drawerOpen }) => scroll,
      drawerOpen: false
    },
    desktop: {
      breakpoint: (theme, query) => query(theme.breakpoints.up("lg")),
      drawerWidth: (theme, query) => 500,
      toolbarHeight: 150,
      main: ({ drawerOpen }) => scroll,
      secondary: ({ drawerOpen }) => scroll,
      drawerOpen: true
    }
  },
}

const PortalIntl: React.FC<{}> = () => {
  const { locale } = Portal.useSite();
  return (<IntlProvider locale={locale} messages={Portal.messages[locale]}>
    <Portal.Provider>{app}</Portal.Provider>
  </IntlProvider>);
}

const PortalApp: React.FC<{}> = () => {
  let defaultLocale = "";
  const browserLocale: string = (navigator.languages && navigator.languages[0]) || navigator.language || (navigator as any).userLanguage || 'en-US';
  if (browserLocale.indexOf("-")) {
    defaultLocale = browserLocale.split("-")[0];
  }
  if (!UI_LANGUAGES.includes(defaultLocale)) {
    defaultLocale = DEFAULT_LOCALE;
  }

  const service = React.useMemo(() => Portal.createService({ defaultLocale, dev: true, content: { url: "http://localhost:8080/q/ide-services" } }), [defaultLocale]);

  return (
    <Portal.SiteProvider service={service} defaultLocale={defaultLocale} overrides={{}}>
      <Portal.TopicsProvider>
        <PortalIntl />
      </Portal.TopicsProvider>
    </Portal.SiteProvider>);
}

export { PortalApp };

