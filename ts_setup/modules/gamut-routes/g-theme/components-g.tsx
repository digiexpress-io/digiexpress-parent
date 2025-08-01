import { Theme } from '@mui/material';
import { SiteApi } from '@dxs-ts/gamut-api';
import { GComponents } from '../g-props';


 
export const components_g: GComponents<Theme> = {
  
  GUserOverview: {
    defaultProps: {
      slotProps: {
        bookings: {
          disabled: false
        }
      }
    }
  },
  GUserOverviewMenu: {
    defaultProps: {
      slotProps: {
        bookings: {
          disabled: false
        }
      }
    }
  },

  GShell: {
    defaultProps: {

      footerHeight: 300,
      drawerWidth: 350,
      toolbarHeight: {
        xs: 155,
        sm: 150,
        md: 90,
        lg: 90,
        xl: 90
      }
    },
    styleOverrides: {
      root: ({  }) => ({

      })
    }
  },

  GMarkdown: {
    defaultProps: {
      children: "# Portal under maintainence",
    },
    styleOverrides: {
      root: ({ theme }) => ({
        '& .MuiTypography-h1': {
          ...theme.typography.h1
        },
        '& :is(h1, h2, h3, h4, h5, p)': {
          marginBottom: theme.spacing(2)
        }
      })
    }
  },

  GArticleFeedback: {
    defaultProps: {
      enabled(view: SiteApi.TopicView) { //015_Palaute
        return view.id.toLowerCase().endsWith('palaute');
      },
    }
  },
}

