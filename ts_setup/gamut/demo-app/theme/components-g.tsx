import { Theme, Components } from '@mui/material';
import user_logo_light from './digi_express_logo.png';
import { DemoFooter } from './DemoFooter';


export const components_g: Components<Omit<Theme, 'components'>> = {

  GLogo: {
    defaultProps: {

    },
    variants: [
      {
        props: { variant: 'black_lg', img: user_logo_light },
        style: { width: '200px', height: 'auto' }
      },
      {
        props: { variant: 'black_sm', img: user_logo_light },
        style: { width: '150px', height: 'auto' }
      },
      {
        props: { variant: 'black_sm_mob', img: user_logo_light },
        style: { width: '120px', height: 'auto' }
      }
    ]
  },
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
      root: ({ theme }) => ({

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

  GUserOverviewDetail: {},

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
  GSort: {},

  // DIALOB FILL
  GFormGroup: {},
  GInputMultilist: {},

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

