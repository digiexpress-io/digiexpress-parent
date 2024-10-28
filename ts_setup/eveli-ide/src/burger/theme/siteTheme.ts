import { createTheme, PaletteOptions, Theme } from "@mui/material/styles";
import {} from "@mui/styles";
declare module 'react' {
  interface CSSProperties {
    '--tree-view-text-color'?: string;
    '--tree-view-color'?: string;
    '--tree-view-bg-color'?: string;
    '--tree-view-hover-color'?: string;
  }
}

declare module '@mui/styles/defaultTheme' {
  interface DefaultTheme extends Theme { }
}

declare module '@mui/material/styles' {
  interface Palette {
    article: Palette['primary'];
    page: Palette['primary'];
    link: Palette['primary'];
    workflow: Palette['primary'];
    release: Palette['primary'];
    locale: Palette['primary'];
    import: Palette['primary'];
    activeItem: Palette['primary'];
    save: Palette['primary'];
    explorer: Palette['primary'];
    explorerItem: Palette['primary'];
    mainContent: Palette['primary'];
    uiElements: Palette['primary'];
    table: Palette['primary'];
  }
  interface PaletteOptions {
    article: Palette['primary'];
    page: Palette['primary'];
    link: Palette['primary'];
    workflow: Palette['primary'];
    release: Palette['primary'];
    locale: Palette['primary'];
    import: Palette['primary'];
    activeItem: Palette['primary'];
    save: Palette['primary'];
    explorer: Palette['primary'];
    explorerItem: Palette['primary'];
    mainContent: Palette['primary'];
    uiElements: Palette['primary'];
    table: Palette['primary'];

  }
}

const palette = {
  mode: 'light',

  primary: {
    main: 'rgb(80, 72, 229)',
    contrastText: '#fff',
    dark: 'rgb(50, 41, 224)',
    light: 'rgb(84, 76, 230)',
  },
  secondary: {
    main: '#3E668E',
    light: '#5585B4',
    dark: '#325171',
    contrastText: '#fff'
  },
  error: {
    main: '#e53935',
  },
  info: {
    main: '##03045E',
    contrastText: '#FFFFFF',
  },
  warning: {
    main: '#ff9800',
    contrastText: '#000000',
  },
  success: {
    main: '#4caf50',
  },
  text: {
    primary: 'rgba(0,0,0,0.86)',
    secondary: 'rgba(0,0,0,0.55)',
    disabled: 'rgba(0,0,0,0.36)',
    hint: 'rgba(0,0,0,0.37)',
  },
  explorer: {
    main: 'rgb(17, 24, 39)', // background colour, dark grey-black
    dark: '',
    light: '',
    contrastText: 'rgb(17, 24, 39)'
  },
  explorerItem: {
    main: 'rgb(209, 213, 219)', // inactive item 
    dark: 'rgb(16, 185, 129)', // active item
    light: 'rgba(255, 255, 255, 0.08)', // active item hover
    contrastText: 'rgba(253, 205, 73)' // indicative item
  },
  mainContent: {
    main: 'rgb(249, 250, 252)', // primary bg colour for behind content boxes, light gray
    dark: 'rgb(18, 24, 40)', // primary content text, dark gray/black
    light: 'rgb(255, 255, 255) ', // primary content bg colour, white
    contrastText: 'rgb(101, 116, 139)' // secondary content text, medium gray
  },
  uiElements: {
    main: 'rgb(80, 72, 229)', // primary ui element, blue-purple (button fill, button text, text, checkboxes, etc.)
    dark: '#F3F4F6', // table header gray
    light: 'rgba(80, 72, 229, 0.04)', // transparent purple for hover backgrounds, secondary button fill on hover
    contrastText: 'rgb(80, 72, 229)'
  },
  table: {
    main: '#F3F4F6', // table header gray
    dark: '#e8eaed', // table header darker gray
    light: '',
    contrastText: '#F3F4F6'
  },
  article: {
    main: '#5048E5', // blue
    dark: '#3229e0',
    light: '#3229e0',
    contrastText: '#fff'
  },
  page: {
    main: '#14B8A6', // turquoise
    dark: '#109384',
    light: '#18dcc5',
    contrastText: '#fff',
  },
  link: {
    main: '#a0548b', // purple
    dark: '#864674',
    light: '#b26c9e',
    contrastText: '#fff'
  },
  workflow: {
    main: '#D14343', // red
    dark: '#c53030',
    light: '#db6b6b',
    contrastText: '#fff'
  },
  release: {
    main: '#91bc24', // green
    dark: '#779a1d',
    light: '#a9d831',
    contrastText: '#fff'
  },
  locale: {
    main: '#FFB020', // orange-yellow
    dark: '#f59f00',
    light: '#ffbf47',
    contrastText: '#fff'
  },
  import: {
    main: 'rgba(77, 144, 142)',
    dark: 'rgba(64, 119, 118)',
    light: 'rgba(86, 159, 158)',
    contrastText: '#fff'
  },
  activeItem: {
    main: '#edf6f9',
    dark: '#edf6f9',
    light: '#edf6f9',
    contrastText: '#000'
  },
  save: {
    main: 'rgba(255, 99, 71, 0.8)',
    dark: 'rgba(255, 183, 3)',
    light: 'rgba(255, 183, 3)',
    contrastText: '#000'
  },

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
      fontWeight: 300,
      paddingTop: 15,
      paddingBottom: 15,
    },
    h4: {
      fontSize: "1.3rem",
      lineHeight: 1,
      fontFamily: "'IBM Plex Sans Arabic', sans-serif",
      fontWeight: 300
    },
    h5: {
      fontSize: "1.1rem",
      fontFamily: "'IBM Plex Sans Arabic', sans-serif",
      fontWeight: 300
    },
    h6: {
      fontFamily: "'IBM Plex Sans Arabic', sans-serif",
      fontWeight: 300
    },
    body1: {
      fontFamily: "'IBM Plex Sans Arabic', sans-serif",
      fontWeight: 300,
    },
    body2: {
      fontFamily: "'IBM Plex Sans Arabic', sans-serif",
      fontSize: "1rem",
    }
  },



  components: {
    MuiCardActions: {
      styleOverrides: {
        root: {

        }
      }
    },
    MuiListItem: {
      styleOverrides: {
        root: {
          paddingTop: 0,
          paddingBottom: 0,
        }
      }
    },

    MuiListItemText: {
      styleOverrides: {
        root: {
          paddingTop: 0,
          paddingBottom: 0,
          marginTop: 0,
          marginBottom: 0,
        },
        primary: {
          color: palette.text.primary,
          "&:hover": {
            color: palette.primary.dark,
            fontWeight: 'bold',
          }
        },
        secondary: {
          fontSize: '.9rem',
          color: palette.text.primary,
          "&:hover": {
            color: palette.primary.dark,
            fontWeight: 'bold',
          }
        }

      }
    },

    MuiButton: {
      styleOverrides: {
        root: {
          fontVariant: 'body2',
          borderRadius: 0,
          textTransform: 'capitalize',
          borderWidth: '2px solid !important',
        }
      },
      defaultProps: {
        variant: 'outlined',
      }
    },

    MuiPaper: {
      styleOverrides: {
        root: {
          elevation: 1,
          borderColor: palette.secondary.main,
          transition: 'unset'
        }
      },
    },
  },

});

export { siteTheme };
