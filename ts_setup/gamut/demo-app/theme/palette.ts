import { PaletteOptions } from "@mui/material";

export const palette: PaletteOptions = {
  primary: {
    main: '#1C7287',         // blue
    dark: '#155566',
    contrastText: '#ffffff',
  },
  secondary: {
    main: 'rgb(240, 169, 169)', // button border or divider colour
    light: 'rgb(0, 126, 143)', // breadcrumbs colour
    dark: 'rgb(3, 50, 47)', // breadcrumbs hover colour
  },
  background: {
    paper: '#f5f5f5',       // light gray for some boxes
    default: '#ffffff',
  },
  text: {
    primary: '#212529',
    secondary: '#65748B',
    disabled: 'rgba(55, 65, 81, 0.48)'
  },
  success: {
    main: '#2e7D32',
    contrastText: '#FFFFFF'
  },
  info: {
    main: '#2196F3',
    light: '#64B6F7',
    dark: '#0B79D0',
    contrastText: '#FFFFFF'
  },
  warning: {
    main: '#FFB020',
    light: '#FFBF4C',
    contrastText: '#000000'
  },
  error: {
    main: '#D32F2F',
    contrastText: '#FFFFFF'
  },

}
