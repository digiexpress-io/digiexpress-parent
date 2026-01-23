import { Theme, Components } from '@mui/material';
import user_logo_light from './digi_express_logo.png';
import city1 from './city1.jpg';
import { DemoFooter } from './DemoFooter';


export const components_g_alt_1: Components<Omit<Theme, 'components'>> = {

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

  GRouterUnsecured: {
    defaultProps: {
      backgroundImage: city1
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

