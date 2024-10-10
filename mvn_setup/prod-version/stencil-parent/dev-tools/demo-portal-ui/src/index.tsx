import React from 'react';
import ReactDOM from 'react-dom';
import { siteTheme } from './theme/siteTheme';
import { ThemeProvider, StyledEngineProvider } from '@mui/material/styles';

import {RefApp} from './RefApp';

ReactDOM.render(
  <React.StrictMode>
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={siteTheme}>
        <RefApp />
      </ThemeProvider>
    </StyledEngineProvider>
  </React.StrictMode>,
  document.getElementById('root')
);
