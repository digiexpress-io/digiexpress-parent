import { Select, SelectProps, generateUtilityClass, styled, useThemeProps, MenuItem, MenuItemProps } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useVariantOverride } from '../api-variants';

export const MUI_NAME = 'GInputList';



// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
export const GInputListRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: { variant: string } }>(({ theme }) => {
  return {

  };
});

// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
export const GInputSelect = styled(Select<string>, {
  name: MUI_NAME,
  slot: 'Input',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      styles.input,
      styles.collapsed,
      useVariantOverride(props, styles),
      props.name,
    ];
  },
})<SelectProps<string>>(({ theme }) => {
  return {
    '& .MuiInputBase-input': {
      paddingTop: theme.spacing(1),
      paddingBottom: theme.spacing(1),
      ...theme.typography.body1
    },

    '& .GInputList-collapsed': {
      display: 'flex',
      width: '100%' 
    },

    // key
    '& .GInputList-collapsed div:nth-of-type(1)': {
      
    },
    // value
    '& .GInputList-collapsed div:nth-of-type(2)': {
      flexGrow: 1
    }
  };
});

export const GInputSelectOption = styled(MenuItem, {
  name: MUI_NAME,
  slot: 'Option',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      styles.option,
      styles.optionKey,
      styles.optionValue,
      styles.optionChecked,

      useVariantOverride(props, styles),
    ];
  },
})<MenuItemProps>(({ theme }) => {
  return {
    '& .GInputList-optionValue': {
      width: '100%'
    },
    '& .GInputList-optionKey': {
      width: '300px'
    },
    '& .GInputList-optionChecked': {
      width: '50px',
      color: theme.palette.primary.main,
      fontSize: 'small',
      marginRight: theme.spacing(1)
    }
  };
});



// ------------------- MATERIAL INFRA, CSS CLASS NAMES FOR SELECTORS -------
export const useUtilityClasses = (itemId: string, variant: string | undefined) => {
  const slots = {
    root: ['root', variant, itemId],
    input: ['input'],
    option: ['option'],
    optionKey: ['optionKey'],
    optionValue: ['optionValue'],
    optionChecked: ['optionChecked'],
    collapsed: ['collapsed'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}