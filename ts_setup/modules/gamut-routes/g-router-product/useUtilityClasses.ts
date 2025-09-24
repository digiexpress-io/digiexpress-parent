import { generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";


export const MUI_NAME = 'GRouterProduct';


export interface GRouterProductClasses {
  root: string;
  productTitle: string;
  productSubTitle: string;
  productBodyText: string;
  formStartButton: string;
  loginAlert: string;
  formAuthButton: string;
  productBreadcrumbs: string;
}

export type GRouterProductClassKey = keyof GRouterProductClasses;



export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    productTitle: ['productTitle'],
    productSubTitle: ['productSubTitle'],
    productBodyText: ['productBodyText'],
    formStartButton: ['formStartButton'],
    loginAlert: ['loginAlert'],
    formAuthButton: ['formAuthButton'],
    productBreadcrumbs: ['productBreadcrumbs'],
    anonBreadcrumbs: ['anonBreadcrumbs']

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}



export const GRouterProductRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.productTitle,
      styles.productSubTitle,
      styles.productBodyText,
      styles.formStartButton,
      styles.loginAlert,
      styles.formAuthButton,
      styles.productBreadcrumbs,
      styles.anonBreadcrumbs
    ];
  },
})(({ theme }) => {
  return {
    '.GRouterProduct-formStartButton': {
      animation: 'pulse 1.5s ease-in-out 5',
      transition: 'transform 0.3s ease-in-out',
      width: '100%',
    },
    '.GRouterProduct-formAuthButton':{
      width: '100%',
    },
    '@keyframes pulse': {
      '0%': { transform: 'scale(1)', opacity: 1 },
      '50%': { transform: 'scale(1.05)', opacity: 0.8 },
      '100%': { transform: 'scale(1)', opacity: 1 },
    },
    '.GRouterProduct-productTitle': {
      textAlign: 'center',
      marginBottom: theme.spacing(3),
      ...theme.typography.h1,
      display: 'flex',
      flexDirection: 'column'
    },
    '.GRouterProduct-productSubTitle': {
      marginBottom: theme.spacing(1),
      ...theme.typography.h3
    },
    '.GRouterProduct-productBodyText': {
      ...theme.typography.body1
    },
    '.GRouterProduct-loginAlert': {
      padding: theme.spacing(3)
    },
    '.GRouterProduct-productBreadcrumbs': {
    },
    '.GRouterProduct-anonBreadcrumbs': {
    },
  }
});



export const GRouterProductButtons = styled("div", {
  name: MUI_NAME,
  slot: 'ProductButtons',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },
})(({ theme }) => {
  return {
    [theme.breakpoints.up('md')]: {
      display: 'flex',
      flexDirection: 'row',
      gap: theme.spacing(1),
    },
    [theme.breakpoints.down('md')]: {
      display: 'flex',
      flexDirection: 'column',
      gap: theme.spacing(1),
      width: '100%'
    },
  }
});





