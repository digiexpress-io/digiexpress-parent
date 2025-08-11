
import { generateUtilityClass, styled } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'
import { useVariantOverride } from '@dxs-ts/gamut-api';
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
})<{ ownerState: GInputGroupProps }>(({ theme, ownerState }) => {
  return {
    ...(ownerState.border ? {
      border: `1px solid ${theme.palette.divider}`,
      padding: theme.spacing(2),
      margin: theme.spacing(1),
      boxShadow: '0px 4px 10px rgba(0, 0, 0, 0.1)'
    } : {})
  };
});





// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
export const GInputGroupLabel = styled('div', {
  name: MUI_NAME,
  slot: 'Label',
  shouldForwardProp: (prop) => prop !== 'onAddRow' && prop !== 'ownerState',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: GInputGroupProps, className: string }>(({ theme, ownerState }) => {

  const { label } = ownerState;
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
    },

    ...(ownerState.disabled ? { // disable the "add" icon 
      '.MuiSvgIcon-root': {
       display: 'none'
      }
   } : {})
  };
});


// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
export const GInputGroupBody = styled('div', {
  name: MUI_NAME,
  slot: 'Body',
  shouldForwardProp: (prop) => prop !== 'onAddRow' && prop !== 'ownerState',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: GInputGroupProps, className: string }>(({ theme }) => {
  return {

  };
});