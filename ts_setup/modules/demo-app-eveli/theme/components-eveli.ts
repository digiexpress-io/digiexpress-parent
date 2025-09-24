import { Theme, Components } from '@mui/material';
import black_log_lg from './digi_express_logo.png';

export const components_eveli: Components<Omit<Theme, 'components'>> = {
  EveliLogo: {
    defaultProps: {},
    variants: [
      {
        props: { variant: 'black_lg', img: black_log_lg },
        style: { width: '160px', height: 'auto' },
      },
    ],
  },

  MuiSnackbarContent: {
    styleOverrides: {
      root: {
        '& .MuiButton-root': {
          color: 'inherit',
          fontWeight: 600,
          textTransform: 'uppercase',
        },
      },
    },
  },       
};
