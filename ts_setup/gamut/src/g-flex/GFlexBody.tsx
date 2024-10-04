import React from 'react';
import { generateUtilityClass, styled, useThemeProps, Paper, lighten } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';


const useUtilityClasses = (ownerState: GFlexBodyProps) => {
  const slots = { root: ['root'], cancel: ['cancel'], header: ['header'] };
  const getUtilityClass = (slot: string) => generateUtilityClass('GFlexBody', slot);
  return composeClasses(slots, getUtilityClass, {});
}


interface GFlexBodyProps {
  children: React.ReactNode;
}



export const GFlexBody: React.FC<GFlexBodyProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: 'GFlexBody',
  });
  const classes = useUtilityClasses(props);
  const ownerState = {
    ...props
  }

  return (
    <GFlexBodyRoot ownerState={ownerState} className={classes.root}>
      {initProps.children}
    </GFlexBodyRoot>
  )
}


const GFlexBodyRoot = styled("div", {
  name: 'GFlexBody',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
    ];
  },
})<{ ownerState: GFlexBodyProps }>(({ theme }) => {
  return {
    backgroundColor: `${lighten(theme.palette.action.disabled, 0.85)}`,
    borderWidth: '1px',
    borderBottomStyle: 'solid',
    borderBottomColor: lighten(theme.palette.action.disabled, 0.5),

    ':hover': {
      backgroundColor: `${lighten(theme.palette.action.disabled, 0.7)}`,
      borderColor: 'rgba(194,190,194,1)',
      boxShadow: '0px 7px 5px -3px rgba(194,190,194,0.7)',
    },

    '& .MuiGrid-root.MuiGrid-container': {
      alignItems: 'center',
      display: 'flex',
      flexDirection: 'row',
      padding: theme.spacing(2),
    },

    // header label hiding
    '& .GFlexBody-header': {
      [theme.breakpoints.up('lg')]: {
        display: 'none'
      }
    },
    // header spacing
    '& .MuiGrid-item:nth-of-type(1)': {
      display: 'flex',
      alignItems: 'center',
      [theme.breakpoints.down('lg')]: {
        marginBottom: theme.spacing(2)
      },
    },

    // content align for big screen
    '& .MuiGrid-item:nth-of-type(n+2)': {
      [theme.breakpoints.up('lg')]: {
        display: 'flex',
        justifyContent: 'flex-end',
      },
    },

    // cancel button styling
    '& .MuiGrid-item:last-of-type': {
      textAlign: 'right',
    },

  };
});


