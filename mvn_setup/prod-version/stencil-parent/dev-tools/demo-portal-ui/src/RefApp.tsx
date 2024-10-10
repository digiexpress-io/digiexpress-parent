import React from 'react';
import { IntlProvider } from 'react-intl'

import { Toolbar } from './toolbar';
import { Primary } from './primary';
import { Secondary } from './secondary';
import { UI_LANGUAGES, DEFAULT_LOCALE } from './util';

import Portal from '@the-stencil-io/portal';


const app: Portal.App = {
  id: "refApp",
  components: { primary: Primary, secondary: Secondary, toolbar: Toolbar },
  config: {
    mobile: {
      drawerWidth: 300,
      toolbarHeight: 100
    },
    tablet: {
      drawerWidth: 400,
      toolbarHeight: 125
    },
    desktop: {
      drawerWidth: 500,
      toolbarHeight: 150
    }
  },
}

const PortalIntl: React.FC<{}> = () => {
  const { locale } = Portal.useSite();
  return (<IntlProvider locale={locale} messages={Portal.messages[locale]}>
    <Portal.Provider>{app}</Portal.Provider>
  </IntlProvider>);
}

const RefApp: React.FC<{}> = (props) => {
  let defaultLocale = "";
  const browserLocale: string = (navigator.languages && navigator.languages[0]) || navigator.language || (navigator as any).userLanguage || 'en-US';
  if (browserLocale.indexOf("-")) {
    defaultLocale = browserLocale.split("-")[0];
  }
  if (!UI_LANGUAGES.includes(defaultLocale)) {
    defaultLocale = DEFAULT_LOCALE;
  }

  const service = React.useMemo(() => Portal.createService({ defaultLocale, dev: false, content: { url: "http://localhost:8080/q/portal/site" } }), [defaultLocale]);

  return (
    <Portal.SiteProvider service={service} defaultLocale={defaultLocale} >
      <PortalIntl />
    </Portal.SiteProvider>);
}

export { RefApp };

