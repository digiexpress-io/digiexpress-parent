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


const siteTheme = createTheme({
  palette: {
    mode: 'light',
  
    primary: {
      main: colors.blue,
      contrastText: '#fff',
      dark: 'rgb(50, 41, 224)',
      light: 'rgb(84, 76, 230)',
    },

    secondary: {
      main: 'rgb(17, 24, 39)', // background colour, dark grey-black
      dark: 'rgb(255, 255, 255)', //unused
      light: 'rgb(253, 205, 73)',
      contrastText: 'rgb(16, 185, 129)'
    },

    error: {
      main: '#e53935',
    },
    
    info: {
      main: '#03045E',
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
      disabled: 'rgba(0,0,0,0.36)'
    }
  },

  typography: {
    fontFamily: '"Ubuntu", sans-serif',
    h1: {
      fontSize: "2rem",
      lineHeight: 2,
      fontWeight: 600,
    },
    h2: {
      fontSize: "1.9rem",
      lineHeight: 1,
      fontWeight: 400,
      paddingTop: 15,
      paddingBottom: 15,
    },
    h3: {
      fontSize: "1.6rem",
      lineHeight: 1,
      fontWeight: 'bold',
      paddingTop: 15,
      paddingBottom: 15,
    },
    h4: {
      fontSize: "1.3rem",
      lineHeight: 1,
      fontWeight: 300
    },
    h5: {
      fontSize: "1.1rem",
      fontWeight: 300
    },
    h6: {
      fontWeight: 300
    },
    body1: {
      fontSize: "1rem",
      fontWeight: 400,
    },
    body2: {
      fontSize: "1rem",
      fontWeight: 400,
    },
    caption: {
      fontSize: "0.7rem",
      fontWeight: 500,
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
              color: "secondary.main",
              "&:focus": { color: colors.blue } 
            },
  
            "& .MuiSvgIcon-root": {
              m: 0,
              color: colors.blue,
              "&:hover": {
                color: "secondary.main"
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
          color: theme.palette.secondary.main, 
          fontWeight: '400'
        })
      }
    },
    MuiDialogTitle: {
      styleOverrides:  {
        root: ({ ownerState, theme }) => {
          return {
            color: theme.palette.primary.contrastText,
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
        /*
        primary: ({theme}) => ({
          color: theme.palette.text.primary,
          "&:hover": {
            color: theme.palette.primary.dark,
            fontWeight: 'bold',
          }
        }),
        secondary: ({theme}) => ({
          fontSize: '.9rem',
          color: theme.palette.text.primary,
          "&:hover": {
            color: theme.palette.primary.dark,
            fontWeight: 'bold',
          }
        })
*/
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
          color: 'rgb(58, 55, 55)', 
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
        root: ({theme}) => ({
          elevation: 1,
          borderColor: theme.palette.secondary.main,
          transition: 'unset'
        })
      },
    },
  },

});

export { siteTheme };
