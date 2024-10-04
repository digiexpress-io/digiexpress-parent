import { Theme, Components, alpha, darken } from '@mui/material';


export const components_mui: Components<Omit<Theme, 'components'>> = {

  MuiCard: {
    styleOverrides: {
      root: ({ theme }) => ({
        backgroundColor: theme.palette.background.default,
        boxShadow: theme.shadows[3],
        '& .MuiCardHeader-root .MuiCardHeader-content .MuiTypography-root': {
          ...theme.typography.h4,
        }
      })
    }
  },
  MuiLink: {
    styleOverrides: {
      root: ({ theme }) => ({
        color: theme.palette.primary.dark,
        textDecoration: 'none',

        ':hover': {
          cursor: 'pointer',
          textDecoration: 'underline',
          color: theme.palette.info.dark
        }
      })
    }
  },

  MuiBreadcrumbs: {
    defaultProps: {
      separator: '/'
    },
    styleOverrides: {
      root: ({ theme }) => ({
        display: 'flex',
        alignItems: 'center',
        padding: theme.spacing(1),
        marginTop: theme.spacing(1),

        '& .MuiLink-root': {
          display: 'flex',
          alignItems: 'center'
        },
        '& .MuiTypography-root': {
          fontSize: theme.typography.body2.fontSize,
          [theme.breakpoints.down('md')]: {
            fontSize: theme.typography.caption.fontSize
          },
        },
        '& .MuiBreadcrumbs-separator': {
          fontSize: theme.typography.body2.fontSize,
          [theme.breakpoints.down('md')]: {
            fontSize: theme.typography.caption.fontSize
          },
        },
        '& .MuiBreadcrumbs-li': {
          borderRadius: theme.spacing(4),
          fontSize: theme.typography.body2.fontSize,
          [theme.breakpoints.down('md')]: {
            fontSize: theme.typography.caption.fontSize
          },
        },
        '& .MuiBreadcrumbs-li:has(p)': {
          fontSize: theme.typography.body2.fontSize,
          color: theme.palette.text.primary,
        },
        '& .MuiBreadcrumbs-li:has(p):last-of-type': {
          fontSize: theme.typography.body2.fontSize,
          color: darken(theme.palette.info.dark, 0.3),
          textDecoration: 'underline',
        },
        '& .MuiSvgIcon-root': {
          fontSize: 'inherit',
          marginRight: theme.spacing(0.5)
        },
      })
    }
  },

  MuiContainer: {
    defaultProps: {
      disableGutters: true,
    },
    styleOverrides: {
      root: ({ theme }) => ({
        padding: theme.spacing(20),

      })
    }
  },
  MuiPaper: {
    defaultProps: {
      variant: 'elevation',
      elevation: 4
    },
    styleOverrides: {
      root: {
        borderRadius: 0,
      }
    }
  },

  MuiSelect: {
    defaultProps: {
      fullWidth: true,
      sx: {
        '.MuiInputBase-input': {
          m: 0,
          px: 1,
          py: 0.5,
          fontSize: '1rem',
          whiteSpace: 'break-spaces',
          overflowWrap: 'break-word',
        }
      }
    },
  },
  MuiTextField: {
    defaultProps: {
      fullWidth: true,

      sx: {
        '.MuiOutlinedInput-input': {
          m: 0,
          px: 2,
          py: 2
        }
      }
    },
  },
  MuiButton: {
    variants: [
      // Main contained button for primary actions
      {
        props: { variant: 'contained' },
        style: ({ theme }) => ({
          fontSize: theme.typography.body1.fontSize,
          textTransform: 'unset',
          borderRadius: 'unset',
          boxShadow: 'unset',
          backgroundColor: theme.palette.primary.main,
          '&:hover': {
            backgroundColor: theme.palette.primary.light,
            boxShadow: 'unset',
          }
        }),
      },

      // Outlined button for secondary actions
      {
        props: { variant: 'outlined' },
        style: ({ theme }) => ({
          fontSize: theme.typography.body1.fontSize,
          textTransform: 'unset',
          borderRadius: 'unset',
          border: `1px solid ${theme.palette.primary.light}`,
          color: theme.palette.primary.dark,
          '&:hover': {
            color: theme.palette.primary.main,
            borderColor: theme.palette.primary.light,
            backgroundColor: alpha(theme.palette.primary.light, 0.05)
          }
        }),
      },

      // Tertiary button
      {
        props: { variant: 'text' },
        style: ({ theme }) => ({
          fontSize: theme.typography.body1.fontSize,
          textTransform: 'unset',
          color: theme.palette.primary.dark,
          '&:hover': {
            color: theme.palette.primary.light,
            backgroundColor: 'unset'
          }
        }),
      },
    ],
  },

  MuiCssBaseline: {
    styleOverrides: (theme: Theme) => ({
      a: {
        textDecoration: 'none',
        color: theme.palette.primary.main,
        fontWeight: 700 // broken, doesnt work 
      }
    })
  }
}