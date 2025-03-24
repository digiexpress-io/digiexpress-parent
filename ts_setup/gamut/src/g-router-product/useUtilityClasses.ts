import { Breadcrumbs, generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";


export const MUI_NAME = 'GRouterProduct';


export interface GRouterProductClasses {
  root: string;
  productTitle: string;
  productSubTitle: string;
  productBodyText: string;
  loginButton: string;
}

export type GRouterProductClassKey = keyof GRouterProductClasses;



export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    productTitle: ['productTitle'],
    productSubTitle: ['productSubTitle'],
    productBodyText: ['productBodyText'],
    productBodyTextError: ['productBodyTextError'],
    formStartButton: ['formStartButton']
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
      styles.formStartButton
    ];
  },
})(({ theme }) => {
  return {
    '.GRouterProduct-formStartButton': {
      animation: 'pulse 1.5s ease-in-out 5',
      transition: 'transform 0.3s ease-in-out',
    },
    '@keyframes pulse': {
      '0%': { transform: 'scale(1)', opacity: 1 },
      '50%': { transform: 'scale(1.05)', opacity: 0.8 },
      '100%': { transform: 'scale(1)', opacity: 1 },
    },
    '.GRouterProduct-productTitle': {
      textAlign: 'center',
      marginBottom: theme.spacing(3),
      ...theme.typography.h1
    },
    '.GRouterProduct-productSubTitle': {
      marginBottom: theme.spacing(1),
      ...theme.typography.h3
    },
    '.GRouterProduct-productBodyText': {
      ...theme.typography.body1
    },
    '.GRouterProduct-productBodyTextError': {

    },
    
  }
});

export const GRouterProductTitleRoot = styled("div", {
  name: MUI_NAME,
  slot: 'ProductTitleRoot',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },
})(({ theme }) => {
  return {
    display: 'flex',
    flexDirection: 'column'
  }
});

export const GRouterProductButtonsRoot = styled("div", {
  name: MUI_NAME,
  slot: 'ProductButtons',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },
})(({ theme }) => {
  return {
    display: 'flex',
    flexDirection: 'row',
    gap: theme.spacing(1),
  }
});

export const GRouterProductBreadcrumbsRoot = styled(Breadcrumbs, {
  name: MUI_NAME,
  slot: 'ProductBreadcrumbs',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },
})(({ theme }) => {
  return {

  }
});

export const GRouterProductAnonBreadcrumbsRoot = styled(Breadcrumbs, {
  name: MUI_NAME,
  slot: 'ProductAnonBreadcrumbs',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },
})(({ theme }) => {
  return {

  }
});



