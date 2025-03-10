import { createTheme, alpha, darken } from "@mui/material/styles";
import { } from "@mui/styles";


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
      main: 'rgb(81, 11, 200)',
      contrastText: '#fff',
      dark: '#D14343',  //colors.red -- red icons
      light: '#a0548b', //colors.purplse -- purple icons
    },

    secondary: {
      main: 'rgb(246, 249, 253)', // Explorer (Secondary) background color
      dark: 'rgb(236, 239, 243)', // Toolbar background color
      light: '#CED8DE', // don't use for dividers! //TODO
      contrastText: 'rgb(16, 185, 129)'
    },

    divider: '#CED8DE', // Borders and dividers

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
      secondary: 'rgb(58, 55, 55)', // Icon and text color
      disabled: 'rgba(0,0,0,0.36)'
    },

    background: {
      default: '#FFFFFF'
    }
  },

  typography: {
    fontFamily: '"Ubuntu", sans-serif',
    h1: {
      fontSize: "2rem",
      lineHeight: 1.5,
      fontWeight: 600,
    },
    h2: {
      fontSize: "1.9rem",
      lineHeight: 1,
      fontWeight: 500,
      paddingTop: 15,
      paddingBottom: 15,
    },
    h3: {
      fontSize: "1.6rem",
      fontWeight: 300,
      lineHeight: 1,
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
        root: ({ ownerState, theme }) => {
          if(ownerState.orientation === "vertical") {
            return {

            }
          }

          return ({
            flexGrow: 1,

            "& .MuiTabs-indicator": {
              backgroundColor: theme.palette.primary.main,
              marginRight: "49px"
            },
  
            "& .MuiTab-root": {
              minHeight: 'unset', 
              color: "secondary.main",
              "&:focus": { color: theme.palette.primary.main } 
            },
  
            "& .MuiSvgIcon-root": {
              m: 0,
              color: theme.palette.primary.main,
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
        root: ({ theme }) => ({
          backgroundColor: theme.palette.secondary.main
        })
      }
    },
    MuiCard: {
      defaultProps: {
        variant: 'outlined'
      },
      styleOverrides: {
        root: ({ theme }) => ({
          borderRadius: 'unset',
          border: `1px solid ${theme.palette.divider}`,
          variant: 'outlined',
        })
      }
    },
    MuiCardHeader: {
      styleOverrides: {
        root: ({theme}) => ({
          backgroundColor: theme.palette.secondary.main,
          padding: theme.spacing(1),
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
        maxWidth: 'md',
      }
    },
    MuiDialogContent: {
      styleOverrides:  {
        root: ({ ownerState, theme }) => ({
          color: theme.palette.secondary.main, 
          fontWeight: '400',
        })
      }
    },
    MuiDialogTitle: {
      styleOverrides:  {
        root: ({ ownerState, theme }) => {
          return {
            ...theme.typography.h1,
            color: theme.palette.primary.main,
            mb: 2, 
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
              borderColor: theme.palette.primary.main,
            },
          },
          '& .MuiSvgIcon-root': {
            color: theme.palette.primary.main
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
            color: theme.palette.primary.main,
            backgroundColor: theme.palette.background.paper,
            '& .MuiOutlinedInput-root': {
              '&.Mui-focused fieldset': {
                borderColor: theme.palette.primary.main,
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
          color: theme.palette.primary.main,
          '&.Mui-checked': {
            color: theme.palette.primary.main,
          }
        })
      },
    },    
    MuiSwitch: {
      styleOverrides:  {
        root: ({ ownerState, theme }) => ({
          '& .MuiSwitch-switchBase.Mui-checked': {
            color: theme.palette.primary.main,
            '&:hover': {
              backgroundColor: alpha(theme.palette.primary.main, 0.1),
            },
          },
          '& .MuiSwitch-switchBase.Mui-checked + .MuiSwitch-track': {
            backgroundColor: alpha(theme.palette.primary.main, 0.5),
          },
        })
      },
    },
    MuiCheckbox: {
      styleOverrides:  {
        root: ({ ownerState, theme }) => ({
          color: theme.palette.primary.main,
          '&.Mui-checked': {
            color: theme.palette.primary.main,
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
            borderRadius: 'unset',
            fontWeight: 'bold',
            backgroundColor: theme.palette.primary.main,
            '&:hover': {
              backgroundColor: darken(theme.palette.primary.main, 0.2),
            },
          }),
        },
        {
          props: { variant: 'text' },
          style: ({ theme }) => ({
            borderRadius: 'unset',
            borderWidth: 0,
            fontWeight: 'bold',
            color: theme.palette.primary.main,
            textTransform: 'capitalize',
            '&:hover': {
              backgroundColor: alpha(theme.palette.primary.main, 0.1),
              border: 'none',
            },
          }),
        },

        {
          props: { variant: 'outlined' },
          style: ({ theme }) => ({
            borderRadius: 'unset',
            borderWidth: 0,
            fontWeight: 'bold',
            color: theme.palette.primary.main,
            textTransform: 'capitalize',
            '&:hover': {
              backgroundColor: alpha(theme.palette.primary.main, 0.1),
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
