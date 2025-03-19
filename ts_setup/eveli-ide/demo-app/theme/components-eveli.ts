import { Theme, Components } from '@mui/material';
import black_log_lg from './black_log_lg.svg';

export const components_eveli: Components<Omit<Theme, 'components'>> = {
  EveliLogo: {
    defaultProps: { },
    variants: [
      {
        props: { variant: 'black_lg', img: black_log_lg },
        style: { width: '160px', height: '45px' }
      }
    ]
  }
}