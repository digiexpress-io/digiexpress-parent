import ReactDOM from 'react-dom';

import { ThemeProvider, StyledEngineProvider } from '@mui/material/styles';
import { IntlProvider } from 'react-intl'
import { siteTheme, StencilComposer, StencilClient, stencilIntl, StencilApi } from '@dxs-ts/eveli-ide';
import '@dxs-ts/eveli-ide/build/style.css';

const locale = "en";

declare global {
  interface Window {
    portalconfig?: { 
      server: { 
        url: string, 
        locked?: boolean,
        oidc?: string,
        status?: string, 
      }  
    },
  }
}

const { portalconfig } = window;

const config: StencilApi.StoreConfig = { 
  url: portalconfig?.server.url ? portalconfig.server.url : "",
  status: portalconfig?.server.status,
  oidc: portalconfig?.server.oidc,
};
console.log("Stencil config", config);

ReactDOM.render(
  <IntlProvider locale={locale} messages={stencilIntl[locale]}>
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={siteTheme}>
        <StencilComposer 
          service={StencilClient.service({config})} 
          locked={portalconfig?.server.locked}/>
      </ThemeProvider>
    </StyledEngineProvider>
  </IntlProvider>
  ,
  document.getElementById('root')
);