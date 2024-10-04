import { Theme, Components } from '@mui/material';
import logoLifeDigitalLight from './logoLifeDigitalLight.svg';
import logoLifeDigitalDark from './logoLifeDigitalDark.svg';
import { DemoFooter } from './DemoFooter';


export const components_g: Components<Omit<Theme, 'components'>> = {

  GShell: {
    defaultProps: {

      footerHeight: 300,
      drawerWidth: 350,
      toolbarHeight: {
        xs: 130,
        sm: 130,
        md: 90,
        lg: 90,
        xl: 90
      }
    }
  },
  GForm: {
    styleOverrides: {
      root: {
        //backgroundColor: "pink"
      },
      variant: [
        {
          props: { variant: 'general-message' },
          style: {
            //border: `2px solid red`,
          },
        }
      ]
    }
  },
  GFormBase: {
    styleOverrides: {
      root: ({ theme }) => ({
        //backgroundColor: "green"
      }),
      variant: [
        {
          props: {
            variant: 'more_specific_subject_area',
          },
          style: {
            //border: `5px solid red`,
          },

        }
      ]
    }
  },

  GOffers: {},
  GContracts: {},
  GLogo: {
    defaultProps: {

    },
    variants: [
      {
        props: { variant: 'white_lg', img: logoLifeDigitalLight },
        style: { width: '300px', height: '100px' }
      },
      {
        props: { variant: 'white_sm', img: logoLifeDigitalLight },
        style: { width: '150px', height: '50px' }
      },
      {
        props: { variant: 'black_lg', img: logoLifeDigitalDark },
        style: { width: '200px', height: '70px' }
      },
      {
        props: { variant: 'black_sm', img: logoLifeDigitalDark },
        style: { width: '150px', height: '50px' }
      },
      {
        props: { variant: 'black_sm_mob', img: logoLifeDigitalDark },
        style: { width: '120px', height: '40px' }
      }
    ]
  },


  GLogin: {},
  GLogout: {},

  GAuth: {
    defaultProps: {
      action: "http://localhost:3000/secured/en/views/user-overview",
    }
  },
  GAuthRepCompany: {
    defaultProps: {
      action: "http://localhost:3000/secured/en/views/user-overview/rep-comp",
      onSubmit: () => {
        console.log('log-in REP-COMPANY');
      }
    }
  },
  GAuthRepPerson: {
    defaultProps: {
      action: "http://localhost:3000/secured/en/views/user-overview/rep-person",
      onSubmit: () => {
        console.log('log-in REP-PERSON');
      }
    }
  },

  GAuthUn: {
    defaultProps: {
      action: "http://localhost:3000/public/en",
      onSubmit: (event) => {
        console.log('log-out');
        event.preventDefault();
        window.location.href = 'http://localhost:3000/public/en'
      }
    }
  },
  GAuthUnRepCompany: {
    defaultProps: {
      action: "http://localhost:3000/secured/en/views/user-overview",
      onSubmit: () => {
        console.log('log-out REP-COMPANY');
      }
    }

  },
  GAuthUnRepPerson: {
    defaultProps: {
      action: "http://localhost:3000/secured/en/views/user-overview",
      onSubmit: () => {
        console.log('log-out REP-PERSON');
      }
    }
  },



  GFooter: {
    defaultProps: {
      children: <DemoFooter />
    },
    styleOverrides: {
      root: ({ theme }) => ({
        [theme.breakpoints.up('md')]: {
          borderTop: `1px solid ${theme.palette.divider}`,
        },
        backgroundColor: theme.palette.background.default,
        color: theme.palette.text.primary,
      })
    }
  },
  GLocales: {
    defaultProps: {
      locales: ['en', 'fi']
    },
     
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
  GUserOverviewMenu: {},
  GUserOverviewDetail: {},
  GUserOverview: {},

  GAppBar: {},
  GArticle: {},
  GBookings: {},

  GPopoverTopics: {},
  GPopoverSearch: {},
  GPopoverButton: {},

  GConfirm: {},

  GLayout: {},
  GLoader: {},

  GServices: {},
  GServicesSearch: {},

  GTooltip: {},

  GInbox: {},
  GInboxMessages: {},
  GInboxAttachments: {},
  GInboxFormReview: {},

  GLinks: {},
  GLinkHyper: {},
  GLinkPhone: {},
  GLinkInfo: {},
  GLinkFormSecured: {},
  GLinkFormUnsecured: {},

  GLinksPage: {}

}

