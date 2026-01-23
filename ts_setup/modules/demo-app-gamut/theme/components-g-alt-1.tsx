import { Theme, Components } from '@mui/material';
import { DemoFooter } from './DemoFooter';
import logo from './NordInvest-no-background.png';
import happyCouple from './couple-on-beach-2-cropped.jpg';
import happyCoupleSmall from './couple-on-beach-2-cropped-small.jpg';

export const components_g_alt_1: Components<Omit<Theme, 'components'>> = {

  GLogo: {
    defaultProps: {

    },
    variants: [
      {
        props: { variant: 'black_lg', img: logo },
        style: { width: '250px', height: 'auto' }
      },
      {
        props: { variant: 'black_sm', img: logo },
        style: { width: '150px', height: 'auto' }
      },
      {
        props: { variant: 'black_sm_mob', img: logo },
        style: { width: '120px', height: 'auto' }
      }
    ]
  },

  GRouterUnsecured: {
    defaultProps: {
      backgroundImage: happyCouple,
      responsiveImages: {
        xs: {
          image: happyCoupleSmall,
          width: '100%',
          height: 250
        },
        sm: {
          image: happyCoupleSmall,
          width: '100%',
          height: 450
        },
        md: {
          image: happyCouple,
          width: '100%',
          height: 350
        },
        lg: {
          image: happyCouple,
          width: '100%',
          height: 500
        },
        xl: {
          image: happyCouple,
          width: '100%',
          height: 500
        },
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
}

