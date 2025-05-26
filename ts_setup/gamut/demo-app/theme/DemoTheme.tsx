import React from 'react';

import { StyledEngineProvider } from "@mui/material/styles";
import { ThemeProvider, createTheme, ThemeOptions } from '@mui/material';

import { components_g } from './components-g';


import { GThemeOptions } from '@dxs-ts/gamut';


export const themeOptions: ThemeOptions = {
  palette: GThemeOptions.palette,
  typography: GThemeOptions.typography,
  components: {
    ...GThemeOptions.components,
    ...components_g,
  }
};
const siteTheme = createTheme(themeOptions);

export const DemoTheme: React.FC<{ children: React.ReactNode }> = ({ children }) => {

  return (
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={siteTheme}>
        {children}
      </ThemeProvider>
    </StyledEngineProvider>);
}

