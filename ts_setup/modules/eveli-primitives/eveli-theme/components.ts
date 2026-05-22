import { Theme, Components, alpha, darken } from '@mui/material';



export const components: Components<Omit<Theme, 'components'>> = {

  MuiTabs: {
    styleOverrides: {
      root: ({ ownerState, theme }) => {
        if (ownerState.orientation === "vertical") {
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
      root: ({ theme }) => ({
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
    styleOverrides: {
      root: ({ ownerState, theme }) => ({
        color: theme.palette.text.primary,
        fontWeight: '400',
      })
    }
  },
  MuiDialogTitle: {
    styleOverrides: {
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
    styleOverrides: {
      root: ({ ownerState, theme }) => ({
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
    styleOverrides: {
      root: ({ ownerState, theme }) => ({
        color: 'rgb(58, 55, 55)',
      })
    }
  },

  MuiRadio: {
    styleOverrides: {
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
    styleOverrides: {
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
    styleOverrides: {
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
          borderWidth: '1px',
          fontWeight: 'bold',
          color: theme.palette.primary.main,
          textTransform: 'capitalize',
          '&:hover': {
            borderWidth: '1px',
          },
        }),
      },
      {
        props: { variant: 'explorerInactive' },
        style: ({ theme }) => ({
          justifyContent: 'left',
          textTransform: 'capitalize',
          marginTop: theme.spacing(0.5),
          borderRadius: theme.spacing(3),
          paddingLeft: theme.spacing(2),
          border: `1px solid ${theme.palette.secondary.main}`,
          ...theme.typography.body1,
          color: theme.palette.text.secondary,
          width: '100%',
          ':hover': {
            backgroundColor: theme.palette.secondary.dark,
            border: `1px solid ${theme.palette.secondary.main}`,
          }
        }),
      },
      {
        props: { variant: 'explorerActive' },
        style: ({ theme }) => ({
          justifyContent: 'left',
          textTransform: 'capitalize',
          marginTop: theme.spacing(0.5),
          borderRadius: theme.spacing(3),
          paddingLeft: theme.spacing(2),
          border: `1px solid ${theme.palette.divider}`,
          ...theme.typography.body1,
          fontWeight: 'bold',
          color: theme.palette.text.secondary,
          width: '100%',
          backgroundColor: theme.palette.secondary.dark,
          '& .MuiSvgIcon-root': {
            color: theme.palette.primary.main
          },
          ':hover': {
            backgroundColor: theme.palette.secondary.dark,
            border: `1px solid ${theme.palette.divider}`,
          }
        }),
      },
    ],
    defaultProps: {
      variant: 'contained',
    }
  },


  MuiPaper: {
    defaultProps: {
      variant: 'outlined',
    },
    styleOverrides: {
      root: ({ theme }) => ({
        borderColor: theme.palette.divider,
        borderRadius: '0px'
      })
    },
  }
}