
import { generateUtilityClass, styled } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'
import { useVariantOverride } from '../api-variants';



export const MUI_NAME = 'GInputBoolean';


export const useUtilityClasses = (itemId: string, variant: string | undefined) => {
  const slots = {
    root: ['root', variant, itemId],
    input: ['input'],
    optionTitle: ['optionTitle'],
    optionIcon: ['optionIcon'],
    option: ['option']
    
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GInputBooleanRoot = styled("div", {
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
    '& .GInputBoolean-input': {
      display: 'flex',
      flexDirection: 'row',
    },
    '& .GInputBoolean-option': {
      paddingTop: theme.spacing(1),
      paddingBottom: theme.spacing(1),

      borderRadius: theme.spacing(0.5),
      display: 'flex',
      justifyContent: 'flex-start'
    },

    '& .GInputBoolean-optionTitle': {
      ...theme.typography.body1,
    },

    '& .GInputBoolean-option:last-of-type': {
      marginLeft: theme.spacing(1),
    },
  };
});


