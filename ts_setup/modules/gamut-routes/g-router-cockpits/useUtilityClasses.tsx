import { Select, SelectProps, generateUtilityClass, styled, MenuItem, MenuItemProps, RadioGroup, RadioGroupProps } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useVariantOverride } from '@dxs-ts/gamut-api';

export const MUI_NAME = 'GCockpitList';


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

    '& .GCockpitList-collapsed': {
      display: 'flex',
      width: '100%' 
    },

    // key
    '& .GCockpitList-collapsed div:nth-of-type(1)': {
      
    },
    // value
    '& .GCockpitList-collapsed div:nth-of-type(2)': {
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
    '& .GCockpitList-optionValue': {
      width: '100%'
    },
    '& .GCockpitList-optionKey': {
      width: '300px'
    },
    '& .GCockpitList-optionChecked': {
      width: '50px',
      color: theme.palette.primary.main,
      fontSize: 'small',
      marginRight: theme.spacing(1)
    }
  };
});



// ------------------- MATERIAL INFRA, CSS CLASS NAMES FOR SELECTORS -------
export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
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