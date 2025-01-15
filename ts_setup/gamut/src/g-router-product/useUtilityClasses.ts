import { Breadcrumbs, generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";


export const MUI_NAME = 'GRouterProduct';


export interface GRouterProductClasses {
  root: string;
  productTitle: string;
  productSubTitle: string;
  productBodyText: string;
}

export type GRouterProductClassKey = keyof GRouterProductClasses;



export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    productTitle: ['productTitle'],
    productSubTitle: ['productSubTitle'],
    productBodyText: ['productBodyText'],
    productBodyTextError: ['productBodyTextError']
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
      styles.productBodyText
    ];
  },
})(({ theme }) => {
  return {

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



