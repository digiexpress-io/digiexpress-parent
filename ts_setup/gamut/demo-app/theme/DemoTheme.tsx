import React from 'react';

import { StyledEngineProvider } from "@mui/material/styles";
import { ThemeProvider, createTheme, ThemeOptions } from '@mui/material';

import { components_g } from './components-g';
import { components_mui } from './components-mui';
import { palette } from './palette';
import { typography } from './typography';


export const themeOptions: ThemeOptions = { palette, typography, components: { ...components_g, ...components_mui } };
const siteTheme = createTheme(themeOptions);


export const DemoTheme: React.FC<{ children: React.ReactNode }> = ({ children }) => {

  return (
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={siteTheme}>
        {children}
      </ThemeProvider>
    </StyledEngineProvider>);
}

