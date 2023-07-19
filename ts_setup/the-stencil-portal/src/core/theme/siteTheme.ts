import { createTheme, PaletteOptions, Theme } from "@mui/material/styles";
import { } from "@mui/styles";


declare module '@mui/styles/defaultTheme' {
  interface DefaultTheme extends Theme { }
}

const palette = {
  mode: 'light',

  primary: {
    main: 'rgb(17, 24, 39)', // dark grey-black
    light: 'rgb(31, 44, 71)', // prussian blue
    dark: 'rgba(6, 9, 14)',
    contrastText: '#ffffff',
  },
  secondary: {
    main: 'rgb(80, 72, 229)', // primary ui element, blue-purple (button fill, button text, text, checkboxes, etc.)
    light: 'rgba(80, 72, 229, 0.04)', // transparent purple for hover backgrounds, secondary button fill
    dark: '#e8eaed', // table header darker gray
    contrastText: 'rgb(101, 116, 139)' // secondary content text, medium gray
  },
  background: {
    default: 'rgb(249, 250, 252)', // primary bg colour for behind content boxes, light gray
    paper: 'rgb(255, 255, 255) ', // primary content bg colour, white
  },
  text: {
    primary: 'rgba(0,0,0,0.86)',
    secondary: 'rgb(209, 213, 219)', // inactive item, gray-ish white
    disabled: 'rgb(209, 213, 219)', // inactive item 
    hint: 'rgba(0,0,0,0.37)',
  },
  error: {
    main: '#f44336',
    light: '#f6685e',
    dark: '#aa2e25',
    contrastText: '#ffffff',
  },
  warning: {
    main: '#ff9800',
    light: '#ffac33',
    dark: '#b26a00',
    contrastText: 'rgba(0,0,0,0.87)',
  },
  info: {
    main: '#554971',
    light: '#796AA0',
    dark: '#413857',
    contrastText: '#ffffff',
  },
  success: {
    main: '#4caf50',
    light: '#6fbf73',
    dark: '#357a38',
    contrastText: 'rgba(0,0,0,0.87)',
  }
}

const siteTheme = createTheme({
  palette: palette as PaletteOptions,

  typography: {
    fontFamily: "'IBM Plex Sans Arabic', sans-serif",
    h1: {
      fontSize: "2rem",
      lineHeight: 2,
      fontFamily: "'IBM Plex Sans Arabic', sans-serif",
      fontWeight: 600,
    },
    h2: {
      fontSize: "1.9rem",
      lineHeight: 1,
      fontFamily: "'IBM Plex Sans Arabic', sans-serif",
      fontWeight: 400,
      paddingTop: 15,
      paddingBottom: 15,
    },
    h3: {
      fontSize: "1.6rem",
      lineHeight: 1,
      fontFamily: "'IBM Plex Sans Arabic', sans-serif",
      fontWeight: 400,
      paddingTop: 15,
      paddingBottom: 15,
    },
    h4: {
      fontSize: "1.3rem",
      lineHeight: 1,
      fontFamily: "'IBM Plex Sans Arabic', sans-serif",
      fontWeight: 400
    },
    h5: {
      fontSize: "1.1rem",
      fontFamily: "'IBM Plex Sans Arabic', sans-serif",
      fontWeight: 400
    },
    h6: {
      fontFamily: "'IBM Plex Sans Arabic', sans-serif",
      fontWeight: 400
    },
    body1: {
      fontFamily: "'IBM Plex Sans Arabic', sans-serif",
      fontWeight: 400,
    },
    body2: {
      fontFamily: "'IBM Plex Sans Arabic', sans-serif",
      fontSize: "1rem",
    }
  },
});

export { siteTheme };
