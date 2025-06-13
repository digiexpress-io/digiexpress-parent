import { PaletteOptions } from "@mui/material";

export const palette: PaletteOptions = {
  mode: 'light',

  primary: {
    main: 'rgb(81, 11, 200)',           // primary color for buttons, active items
    contrastText: '#fff',
    dark: '#D14343',                      // colors.red for red icons
    light: '#a0548b',                     // colors.purple for purple icons
  },

  secondary: {
    main: 'rgb(246, 249, 253)',         // Explorer (Secondary) background color
    dark: 'rgb(236, 239, 243)',         // Toolbar background color
    light: '#CED8DE',                     // don't use for dividers! //TODO
    contrastText: 'rgb(16, 185, 129)'
  },

  divider: '#CED8DE',                     // Borders and dividers

  error: {
    main: '#e53935',
  },

  info: {
    main: '#03045E',
    contrastText: '#FFFFFF',
  },
  warning: {
    light: '#ffeb3b',
    main: '#ffc107',
    dark: '#ffa000',
    contrastText: '#000000' 
  },
  success: {
    main: '#009900',
    light: '#00ff00'
  },

  text: {
    primary: 'rgba(0,0,0,0.86)',
    secondary: 'rgb(58, 55, 55)', // Icon and text color
    disabled: 'rgba(0,0,0,0.36)'
  },

  background: {
    default: '#FFFFFF'
  }
}
