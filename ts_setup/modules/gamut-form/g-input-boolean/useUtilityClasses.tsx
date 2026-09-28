
import { generateUtilityClass, styled } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'
import { useVariantOverride } from '@dxs-ts/gamut-api';



export const MUI_NAME = 'GInputBoolean';

interface OwnerState {
  variant: string,
  disabled: boolean,
  readOnly?: boolean,
}


export const useUtilityClasses = (itemId: string, variant: string | undefined) => {
  const slots = {
    root: ['root', variant, itemId],
    input: ['input'],
    optionTitle: ['optionTitle'],
    singleCheckboxTitle: ['singleCheckboxTitle'],
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
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => {



  return {
    ...(ownerState.disabled) ? {
      '& .MuiSvgIcon-root': {
        color: theme.palette.info.main
      },
      '& .MuiButtonBase-root.Mui-disabled': {
        color: theme.palette.info.main,
        backgroundColor: theme.palette.background.paper,
        border: `1px solid ${theme.palette.action.disabled}`,
        '& .MuiTypography-root': {
          color: theme.palette.info.main,
        }
      },
    } : {},

    ...(ownerState.readOnly) ? {
      '& .GInputBoolean-input': {
        cursor: 'not-allowed',
      },
      '& .MuiButtonBase-root, & .MuiButtonBase-root *': {
        pointerEvents: 'none',
      },
    } : {},

    ...(ownerState.variant === 'singleCheckbox') ? {
      '& .GInputBase-label': {
        display: 'none',
      },
      '& .GInputBase-root': {
        justifyContent: 'flex-end',
      },
      '& .GInputBoolean-singleCheckboxTitle': {
        ...theme.typography.body1,
        textAlign: 'left',
        width: '100%',
      },
    } : {},

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
      marginLeft: ownerState.variant === 'singleCheckbox' ? 0 : theme.spacing(1),
    },
  };
});


