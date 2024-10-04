
import { generateUtilityClass, styled, Typography } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'



export const MUI_NAME = 'GLinks';


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}



export const GLinksRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.title
    ];
  },
})(({ theme }) => {
  return {
    [theme.breakpoints.up('md')]: {
      borderLeft: `3px solid ${theme.palette.primary.main}`,
      marginTop: theme.spacing(2),
      paddingLeft: theme.spacing(1),
    },
    [theme.breakpoints.down('md')]: {
      borderBottom: `2px solid ${theme.palette.primary.main}`,
      padding: theme.spacing(2),
    },
    '& .GLinkHyper-root': {
      marginTop: theme.spacing(1),
      marginBottom: theme.spacing(1),
    },
    '& .GLinkPhone-root': {
      marginTop: theme.spacing(1),
      marginBottom: theme.spacing(1)
    },
    '& .GLinkFormSecured-root': {
      marginTop: theme.spacing(1),
      marginBottom: theme.spacing(1),
    },
    '& .GLinkFormUnsecured-root': {
      marginTop: theme.spacing(1),
      marginBottom: theme.spacing(1),
    },
    '& a': {
      fontWeight: theme.typography.fontWeightMedium,
    },
  };
});

export const GLinksTitle = styled(Typography, {
  name: MUI_NAME,
  slot: 'Title',
  overridesResolver: (_props, styles) => {
    return [
      styles.header,
    ];
  },
})(({ theme }) => {
  return {
    ...theme.typography.h3
  };
});