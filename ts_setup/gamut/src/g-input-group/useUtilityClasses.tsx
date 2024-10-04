
import { generateUtilityClass, styled } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'
import { useVariantOverride } from '../api-variants';
import { GInputGroupProps } from './GInputGroup';


export const MUI_NAME = 'GInputGroup';


export const useUtilityClasses = (itemId: string) => {
  const slots = {
    root: ['root', itemId],
    label: ['label'],
    body: ['body']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GInputGroupRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'onAddRow' && prop !== 'ownerState',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: {  } }>(({ theme }) => {
  return {
    
  };
});





// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
export const GInputGroupLabel = styled('div', {
  name: MUI_NAME,
  slot: 'Label',
  shouldForwardProp: (prop) => prop !== 'onAddRow',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },
})<GInputGroupProps>(({ theme, label }) => {
  return {
    display: 'flex',
    '& .MuiDivider-root': {
      display: label ? undefined : 'none',
      flexGrow: 1,
      alignSelf: 'center',
      marginLeft: theme.spacing(2)
    },
    '& .MuiTypography-root': {
      ...theme.typography.h3
    }
  };
});


// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
export const GInputGroupBody = styled('div', {
  name: MUI_NAME,
  slot: 'Body',
  shouldForwardProp: (prop) => prop !== 'onAddRow',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },
})<GInputGroupProps>(({ theme }) => {
  return {

  };
});