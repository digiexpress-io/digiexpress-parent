
import { generateUtilityClass, styled } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'
import { useVariantOverride } from '../api-variants';



export const MUI_NAME = 'GInputDate';


export const useUtilityClasses = (itemId: string, variant: string | undefined) => {
  const slots = {
    root: ['root', variant, itemId],
    input: ['input'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}



export const GInputDateRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: { variant: string, disabled: boolean } }>(({ theme, ownerState }) => {

  if (ownerState.disabled) {
    return {
      '& .MuiInputBase-root': {
        backgroundColor: theme.palette.background.paper
      },
      '& .react-date-picker__inputGroup__input': {
        color: theme.palette.info.main,
      },
      '& .react-date-picker__inputGroup__leadingZero, & .react-date-picker__inputGroup__divider': {
        color: theme.palette.info.main,
      },
      '& .MuiSvgIcon-root': { // disable the icons in the input field
        display: 'none'
      }
    }
  }

});


export const GInputDateInput = styled("div", {
  name: MUI_NAME,
  slot: 'Input',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      useVariantOverride(props, styles),
    ];
  },
})<{ ownerState: { variant: string } }>(({ theme, ownerState }) => {


  return {
    '& .react-date-picker': {
      alignItems: 'center',
      width: '100%',
      boxSizing: 'border-box', // Prevent padding issue with fullWidth.
      padding: '4px 0 5px',
      border: '1px solid rgba(0, 0, 0, 0.23)',
      outline: '1px solid rgb(0,0,0, 0.0)',
      borderRadius: theme.spacing(0.5),
    },

    '& .react-date-picker__wrapper': {
      border: 'unset'
    },

    '& .react-date-picker__inputGroup__input': {
      ...theme.typography.body1
    },


  };
});
