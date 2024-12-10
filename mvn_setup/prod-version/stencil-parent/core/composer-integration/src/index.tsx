import ReactDOM from 'react-dom';
import { createBrowserRouter, createRoutesFromElements, Navigate, Route, RouterProvider, Outlet, useMatch } from 'react-router-dom';
import { ThemeProvider, StyledEngineProvider } from '@mui/material/styles';
import { IntlProvider } from 'react-intl'

import { siteTheme, StencilComposer, StencilClient, stencilIntl, StencilApi } from '@dxs-ts/eveli-ide';


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

const StartRouter: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const basename = '';
  return <RouterProvider router={createBrowserRouter(createRoutesFromElements(children), { basename })} />
}




ReactDOM.render(
  <IntlProvider locale={locale} messages={stencilIntl[locale]}>
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={siteTheme}>
<StartRouter>
  <Route path='/' element={<StencilComposer 
            service={StencilClient.service({config})} 
            locked={portalconfig?.server.locked}/>}>    
  </Route>
</StartRouter>
      </ThemeProvider>
    </StyledEngineProvider>
  </IntlProvider>
  ,
  document.getElementById('root')
);