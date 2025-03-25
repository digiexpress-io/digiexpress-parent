import { Theme, Components, alpha } from '@mui/material';
import user_logo_light from './logoLifeDigitalDark.svg';
import { DemoFooter } from './DemoFooter';


export const components_g: Components<Omit<Theme, 'components'>> = {

  GLogo: {
    defaultProps: {

    },
    variants: [
      {
        props: { variant: 'black_lg', img: user_logo_light },
        style: { width: '200px', height: '70px' }
      },
      {
        props: { variant: 'black_sm', img: user_logo_light },
        style: { width: '150px', height: '50px' }
      },
      {
        props: { variant: 'black_sm_mob', img: user_logo_light },
        style: { width: '120px', height: '40px' }
      }
    ]
  },

  GShell: {
    defaultProps: {

      footerHeight: 300,
      drawerWidth: 350,
      toolbarHeight: {
        xs: 150,
        sm: 150,
        md: 90,
        lg: 90,
        xl: 90
      }
    },
    styleOverrides: {
      root: ({ theme }) => ({
        backgroundColor: theme.palette.primary.main,
        // border around everything
        // borderRight: `1px solid ${theme.palette.divider}`,
        // borderLeft: `1px solid ${theme.palette.divider}`,

        // margin around page  
        [theme.breakpoints.up('md')]: {
          paddingLeft: theme.spacing(35),
          paddingRight: theme.spacing(35),
        },

        // margin around drawer on secured page
        '.MuiDrawer-root.GShellBase .MuiPaper-root': {
          // because drawer has position fixed we need to duplicate the margin
          marginLeft: theme.spacing(35),
          // border on left of drawer
          borderLeft: `1px solid ${theme.palette.divider}`,
        },
        // remove box shadow on the sides of appBar without removing it on bottom
        '& .MuiToolbar-root.GShellBase': {
          boxShadow: `0 4px 6px -1px ${alpha(theme.palette.text.primary, 0.2)},  0 2px 4px -1px ${alpha(theme.palette.text.primary, 0.1)}`,
        }
      })
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
  GRouterBookings: {},
  GRouterFormsAwaitingDecision: {},
  GRouterFormsWithDecision: {},
  GRouterInbox: {},
  GRouterInboxSubject: {},
  GRouterOffer: {},
  GRouterOfferSummary: {},
  GRouterProduct: {},
  GRouterSecuredServices: {},
  GRouterUnfinishedForms: {},
  GRouterUnsecured: {},
  GRouterUserOverview: {},

  GLogin: {},
  GLogout: {},

  GAuth: {
    defaultProps: {
      action: "http://localhost:3001/secured/en/views/user-overview",
    }
  },
  GAuthFormStart: {
    defaultProps: {
      action: "/portal/login",
    }
  },
  GAuthRepCompany: {
    defaultProps: {
      action: "http://localhost:3001/secured/en/views/user-overview/rep-comp",
      onSubmit: () => {
        console.log('log-in REP-COMPANY');
      }
    }
  },
  GAuthRepPerson: {
    defaultProps: {
      action: "http://localhost:3001/secured/en/views/user-overview/rep-person",
      onSubmit: () => {
        console.log('log-in REP-PERSON');
      }
    }
  },

  GAuthUn: {
    defaultProps: {
      action: "http://localhost:3001/public/en",
      onSubmit: (event) => {
        console.log('log-out');
        event.preventDefault();
        window.location.href = 'http://localhost:3001/public/en'
      }
    }
  },
  GAuthUnRepCompany: {
    defaultProps: {
      action: "http://localhost:3001/secured/en/views/user-overview",
      onSubmit: () => {
        console.log('log-out REP-COMPANY');
      }
    }

  },
  GAuthUnRepPerson: {
    defaultProps: {
      action: "http://localhost:3001/secured/en/views/user-overview",
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

  GArticleFeedback: {
    defaultProps: {
      enabled(view) { //015_Palaute
        return view.id.toLowerCase().endsWith('palaute');
      },
    }
  },

  GUserOverviewMenu: {},
  GUserOverviewDetail: {},
  GUserOverview: {},

  GAppBar: {},
  GArticle: {},
  GArticleFeedbackViewer: {},
  GBookings: {},

  GPopoverTopics: {},
  GPopoverSearch: {},
  GPopoverButton: {},

  GConfirm: {},

  GLayout: {},
  GLoader: {},

  GSecuredServices: {},
  GSecuredServicesSearch: {},

  GTooltip: {},

  GInbox: {},
  GInboxMessages: {},
  GInboxMessageNotAllowed: {},
  GInboxAttachments: {},
  GInboxFormReview: {},

  GLinks: {},
  GLinkHyper: {},
  GLinkPhone: {},
  GLinkInfo: {},
  GLinkFormLocked: {},
  GLinkFormUnlocked: {},

  GLinksPage: {},

  // ---------------------- DIALOB REVIEW ------------------------
  GFormReviewBoolean: {},
  GFormReviewChoice: {},
  GFormReviewDate: {},
  GFormReviewMultiChoice: {},
  GFormReviewDecimal: {},
  GFormReviewGroup: {},
  GFormReviewItem: {},
  GFormReviewNote: {},
  GFormReviewPage: {},
  GFormReviewQuestionnaire: {},
  GFormReviewRowGroup: {},
  GFormReviewSurvey: {},
  GFormReviewSurveyGroup: {},
  GFormReviewText: {},
  GFormReviewTime: {}
}

