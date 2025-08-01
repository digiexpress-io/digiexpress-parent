import React from 'react';
import { Grid, styled } from '@mui/material';



export interface GFlexHeaderProps {
  children: React.ReactNode;
}
export const GFlexHeader: React.FC<GFlexHeaderProps> = (props) => {
  const { children } = props;

  return (
    <GFlexHeaderRoot container>
      {children}
    </GFlexHeaderRoot>
  )
}

const GFlexHeaderRoot = styled(Grid, {
  name: 'GFlexHeader',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
    ];
  },
})(({ theme }) => {
  return {
    [theme.breakpoints.between('xs', 'lg')]: {
      display: 'none'
    },
    padding: theme.spacing(2),
    alignItems: 'center',

    '& .MuiTypography-root': {
      ...theme.typography.body2,
      fontWeight: 'bold'
    },
    '& .MuiGrid-item:nth-of-type(1)': {
      display: 'flex',
      justifyContent: 'flex-start',
    },
    '& .MuiGrid-item:nth-of-type(n+2)': {
      display: 'flex',
      justifyContent: 'flex-end',
    },
  };
});
