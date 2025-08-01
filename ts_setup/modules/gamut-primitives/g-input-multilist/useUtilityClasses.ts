import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useVariantOverride } from '../g-variants';
import { GInputMultilistProps } from './g-input-multilist-types';


export const MUI_NAME = 'GInputMultilist';


// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
export const GInputMultilistRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: GInputMultilistProps }>(({ theme, ownerState }) => {

  return {
    ...(ownerState.border ? {
      border: `1px solid ${theme.palette.divider}`,
      padding: theme.spacing(2),
      margin: theme.spacing(1),
      boxShadow: '0px 4px 10px rgba(0, 0, 0, 0.1)'
    } : {}),

    ...(ownerState.disabled ? {
      '& .MuiInputBase-root.Mui-disabled': {
        backgroundColor: theme.palette.background.paper,
      },
      '& .MuiChip-root.Mui-disabled': {
        opacity: '1',
        color: `${theme.palette.info.main}`,
        '.MuiSvgIcon-root': { //disable the "close" icon in chip
          display: 'none'
        },

      }
    } : {})

  };
})


// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
export const GInput = styled('div', {
  name: MUI_NAME,
  slot: 'Input',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      useVariantOverride(props, styles),
      props.name,
    ];
  },
})(({ theme }) => {
  return {
    display: 'flex',
    flexDirection: 'row',

    '& .GInputMultilist-list': {
      display: 'flex',
      flexDirection: 'column',
      width: '100%'
    },

    '& .GInputMultilist-option': {
      borderRadius: theme.spacing(0.5),

      paddingTop: theme.spacing(1),
      paddingBottom: theme.spacing(1),

      display: 'flex',
      justifyContent: 'flex-start'
    },

    '& .GInputMultilist-option:not(:last-child) ': {
      marginBottom: theme.spacing(2),
    },

    '& .GInputMultilist-title': {
      ...theme.typography.body1
    },
  };
});

// ------------------- MATERIAL INFRA, CSS CLASS NAMES FOR SELECTORS -------
export const useUtilityClasses = (itemId: string, variant: string | undefined) => {
  const slots = {
    root: ['root', variant, itemId],
    input: ['input'],
    list: ['list'],
    optionTitle: ['optionTitle'],
    optionIcon: ['optionIcon'],
    option: ['option']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}