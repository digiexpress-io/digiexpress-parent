import { createTheme, PaletteOptions, Theme, alpha, darken } from "@mui/material/styles";
import {} from "@mui/styles";
import { colors } from '../eveli-colors';


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

    page: Palette['primary'];
    link: Palette['primary'];
    release: Palette['primary'];
    locale: Palette['primary'];
    import: Palette['primary'];
    activeItem: Palette['primary'];
    save: Palette['primary'];
    explorer: Palette['primary'];
    explorerItem: Palette['primary'];
    mainContent: Palette['primary'];
  }
  interface PaletteOptions {
    page: Palette['primary'];
    link: Palette['primary'];
    release: Palette['primary'];
    locale: Palette['primary'];
    import: Palette['primary'];
    activeItem: Palette['primary'];
    save: Palette['primary'];
    explorer: Palette['primary'];
    explorerItem: Palette['primary'];
    mainContent: Palette['primary'];

  }
}

const palette = {
  mode: 'light',

  primary: {
    main: colors.blue,
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
    dark: 'rgb(255, 255, 255)',
    light: 'rgb(18, 24, 40)',
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
    light: 'rgb(255, 255, 255)', // primary content bg colour, white
    contrastText: 'rgb(101, 116, 139)' // secondary content text, medium gray
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
      fontWeight: 'bold',
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

    MuiTabs: {
      styleOverrides: {
        root: ({ ownerState }) => {
          if(ownerState.orientation === "vertical") {
            return {

            }
          }

          return ({
            flexGrow: 1,

            "& .MuiTabs-indicator": {
              backgroundColor: colors.blue,
              marginRight: "49px"
            },
  
            "& .MuiTab-root": {
              minHeight: 'unset', 
              color: "mainContent.dark",
              "&:focus": { color: colors.blue } 
            },
  
            "& .MuiSvgIcon-root": {
              m: 0,
              color: colors.blue,
              "&:hover": {
                color: "mainContent.dark"
              }
            }
  
          })
        }
      }
    },


    MuiTableHead: {
      styleOverrides: {
        root: () => ({
          backgroundColor: colors.grey
        })
      }
    },
    MuiCardHeader: {
      styleOverrides: {
        root: ({theme}) => ({
          backgroundColor: colors.grey,
          padding: theme.spacing(1)
        })
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

    MuiDialog: {
      defaultProps: {
        fullWidth: true, 
        maxWidth: 'md'
      }
    },
    MuiDialogContent: {
      styleOverrides:  {
        root: ({ ownerState, theme }) => ({
          color: theme.palette.mainContent.dark, 
          fontWeight: '400'
        })
      }
    },
    MuiDialogTitle: {
      styleOverrides:  {
        root: ({ ownerState, theme }) => {


          return {
            color: theme.palette.secondary.contrastText,
            fontWeight: 'bold',
            borderBottom: '1px solid gray',
            mb: 2, 
            backgroundColor: alpha(colors.blue, 0.9)
          }
        }
      }
    },

    MuiTextField: {
      styleOverrides:  {
        root: ({ ownerState, theme }) => ({
          marginTop: theme.spacing(1),
          color: theme.palette.primary.contrastText,
          backgroundColor: theme.palette.background.paper,
          '& .MuiInputBase-input': {
            padding: theme.spacing(2),
            color: ownerState.value ? theme.palette.text.primary : theme.palette.text.secondary,
          },
          '& .MuiOutlinedInput-root': {
            '&.Mui-focused fieldset': {
              borderColor: colors.blue,
            },
          },
          '& .MuiSvgIcon-root': {
            color: colors.blue
          }
        })
      },
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

    MuiFormControl: {
      variants: [
        {
          props: { variant: 'outlined' },
          style: ({ theme }) => ({
            marginTop: theme.spacing(2),
            color: colors.blue,
            backgroundColor: theme.palette.background.paper,
            '& .MuiOutlinedInput-root': {
              '&.Mui-focused fieldset': {
                borderColor: colors.blue,
              },
            }
          }), 
        }
      ]
    },

    MuiIconButton: {
      styleOverrides:  {
        root: ({ ownerState, theme }) => ({
          color: colors.blue, 
        })
      }
    },

    MuiRadio: {
      styleOverrides:  {
        root: ({ ownerState, theme }) => ({
          marginLeft: theme.spacing(1.5),
          color: colors.blue,
          '&.Mui-checked': {
            color: colors.blue,
          }
        })
      },
    },    
    MuiSwitch: {
      styleOverrides:  {
        root: ({ ownerState, theme }) => ({
          '& .MuiSwitch-switchBase.Mui-checked': {
            color: colors.blue,
            '&:hover': {
              backgroundColor: alpha(colors.blue, 0.1),
            },
          },
          '& .MuiSwitch-switchBase.Mui-checked + .MuiSwitch-track': {
            backgroundColor: alpha(colors.blue, 0.5),
          },
        })
      },
    },
    MuiCheckbox: {
      styleOverrides:  {
        root: ({ ownerState, theme }) => ({
          color: colors.blue,
          '&.Mui-checked': {
            color: colors.blue,
          }
        })
      },
    },

    MuiButton: {
      variants: [
        {
          props: { variant: 'contained' },
          style: ({ theme }) => ({
            fontVariant: 'body2',
            textTransform: 'capitalize',
            borderWidth: '2px solid !important',
            borderRadius: theme.spacing(1),
            fontWeight: 'bold',
            backgroundColor: colors.blue,
            '&:hover': {
              backgroundColor: darken(colors.blue, 0.2),
            },
          }),
        },
        {
          props: { variant: 'text' },
          style: ({ theme }) => ({
            borderRadius: theme.spacing(1),
            borderWidth: 0,
            fontWeight: 'bold',
            color: colors.blue,
            textTransform: 'capitalize',
            '&:hover': {
              backgroundColor: alpha(colors.blue, 0.1),
              border: 'none',
            },
          }),
        },

        {
          props: { variant: 'outlined' },
          style: ({ theme }) => ({
            borderRadius: theme.spacing(1),
            borderWidth: 0,
            fontWeight: 'bold',
            color: colors.blue,
            textTransform: 'capitalize',
            '&:hover': {
              backgroundColor: alpha(colors.blue, 0.1),
              border: 'none',
            },
          }),
        },
      ],
      defaultProps: {
        variant: 'contained',
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
