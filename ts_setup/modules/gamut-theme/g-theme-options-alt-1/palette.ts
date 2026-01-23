import { PaletteOptions } from "@mui/material";

export const palette: PaletteOptions = {
  primary: {
    main: '#881b1bff',         // blue
    dark: '#de1616ff',
    contrastText: '#de1616ff',
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
    primary: '#de1616ff',
    secondary: '#de1616ff',
    disabled: '#de1616ff'
  },
  action: {
    disabled: '#de1616ff',
    active: '#de1616ff' // Mui input adornments inherit this colour -- needed for dialob fill
  },
  divider: "#dee2e6",
  success: {
    main: '#2e7D32',
    contrastText: '#FFFFFF'
  },
  info: {
    main: '#0000CC',
    light: '#3333FF',
    dark: '#000099',
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
