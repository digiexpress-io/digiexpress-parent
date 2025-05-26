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
}

