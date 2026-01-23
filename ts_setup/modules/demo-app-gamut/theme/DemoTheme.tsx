import React from 'react';

import { StyledEngineProvider } from "@mui/material/styles";
import { ThemeOptions } from '@mui/material';

import { components_g } from './components-g';


import { GThemeOptions, GThemeOptionsAlt1, GThemeProvider } from '@dxs-ts/gamut';
import { components_g_alt_1 } from './components-g-alt-1';


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
    ...components_g_alt_1,
  }
}

export const DemoTheme: React.FC<{ children: React.ReactNode }> = ({ children }) => {

  return (
    <StyledEngineProvider injectFirst>
      <GThemeProvider themeOptions={themeOptions} secondaryThemeOptions={{ 
        'cockpit 1': themeOptionsAlt1,
        'test 1': themeOptionsAlt1,
        'life insurance': themeOptionsAlt1
      }}>
        {children}
      </GThemeProvider>
    </StyledEngineProvider>);
}

