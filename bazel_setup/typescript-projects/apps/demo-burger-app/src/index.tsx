import React from 'react';
import ReactDOM from 'react-dom';
import { ThemeProvider, StyledEngineProvider } from '@mui/material/styles';
import { IntlProvider } from 'react-intl';

import { siteTheme } from '@digiexpress/react-burger';
import { DemoBurger } from './test_app';

ReactDOM.render(
  <React.StrictMode>
    <IntlProvider locale="en" messages={{}}>
      <StyledEngineProvider injectFirst>
        <ThemeProvider theme={siteTheme}>
          <DemoBurger />
        </ThemeProvider>
      </StyledEngineProvider>
    </IntlProvider>
  </React.StrictMode>,
  document.getElementById('root')
);
