import { generateUtilityClass, styled, useThemeProps, Menu } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useVariantOverride } from '../api-variants';
import { GFormPageProps } from './GFormPage';

const MUI_NAME = 'GFormPage';


export function useThemeInfra(initProps: GFormPageProps) {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(props);
  const ownerState = { ...props };
  return { classes, ownerState, props };
}


// ------------------- MATERIAL INFRA, CSS CLASS NAMES FOR SELECTORS -------
const useUtilityClasses = (ownerState: GFormPageProps) => {
  const slots = {
    root: ['root', ownerState.id],
    header: ['header'],
    titles: ['titles'],
    title: ['title'],
    subTitle: ['subTitle'],

    body: ['body'],
    footer: ['footer'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}



// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
export const GFormPageRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: GFormPageProps }>(({ theme }) => {
  return {


  };
});


export const GFormPageHeader = styled('div', {
  name: MUI_NAME,
  slot: 'Body',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: GFormPageProps }>(({ theme }) => {
  return {
    display: 'flex',
    flexDirection: 'row',
    '& .GFormPage-titles': {
      flexGrow: 1 
    }
  };
});


export const GFormPageTitle = styled('div', {
  name: MUI_NAME,
  slot: 'Title',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: GFormPageProps }>(({ theme }) => {
  return {
    '& .MuiTypography-root': {
      ...theme.typography.h2
    }
  };
});
export const GFormPageSubTitle = styled('div', {
  name: MUI_NAME,
  slot: 'Title',
  overridesResolver: (props, styles) => {
    return [
      styles.root, ...useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: GFormPageProps }>(({ theme, ownerState }) => {
  return {
    display: ownerState.subTitle ? 'flex' : 'none',
    alignItems: 'center',
    '& .MuiTypography-root': {
      ...theme.typography.h4,
      color: theme.palette.text.disabled,
    },
    '& .MuiIconButton-root': {
      color: theme.palette.text.disabled,
    }
  };
});




// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
export const GFormPageBody = styled('div', {
  name: MUI_NAME,
  slot: 'Body',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: GFormPageProps }>(({ theme }) => {
  return {

  };
});

export const GFormPageMenu = styled(Menu, {
  name: MUI_NAME,
  slot: 'Menu',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: GFormPageProps }>(({ theme }) => {
  return {
    
    '& .MuiListItemIcon-root': {
      paddingRight: theme.spacing(1)
    },
    '& .MuiPaper-root': {
      borderRadius: theme.spacing(1)
    }
  };
});

export const GFormPageFooter = styled('div', {
  name: MUI_NAME,
  slot: 'Footer',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: GFormPageProps }>(({ theme }) => {
  return {

  };
});