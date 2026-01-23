import React from 'react';

import { StyledEngineProvider } from "@mui/material/styles";
import { ThemeOptions } from '@mui/material';

import { components_g } from './components-g';


import { GThemeOptions, GThemeOptionsAlt1, GThemeProvider } from '@dxs-ts/gamut';


export const themeOptions: ThemeOptions = {
  palette: GThemeOptions.palette,
  typography: GThemeOptions.typography,
  components: {
    ...GThemeOptions.components,
    ...components_g,
  }
}

export const themeOptionsAlt1: ThemeOptions = {
  palette: GThemeOptionsAlt1.palette,
  typography: GThemeOptionsAlt1.typography,
  components: {
    ...GThemeOptionsAlt1.components,
    ...components_g,
  }
}

export const DemoTheme: React.FC<{ children: React.ReactNode }> = ({ children }) => {

  return (
    <StyledEngineProvider injectFirst>
      <GThemeProvider themeOptions={themeOptions} secondaryThemeOptions={{ 'test 1': themeOptionsAlt1 }}>
        {children}
      </GThemeProvider>
    </StyledEngineProvider>);
}

