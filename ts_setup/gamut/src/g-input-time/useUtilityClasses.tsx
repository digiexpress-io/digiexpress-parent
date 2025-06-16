
import { generateUtilityClass, styled } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'
import { useVariantOverride } from '../api-variants';



export const MUI_NAME = 'GInputTime';


export const useUtilityClasses = (itemId: string, variant: string | undefined) => {
  const slots = {
    root: ['root', variant, itemId],
    input: ['input'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}



export const GInputTimeRoot = styled("div", {
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
      color: theme.palette.info.main,
      '& .MuiInputBase-root': {
        backgroundColor: theme.palette.background.paper,
        border: `1px solid ${theme.palette.action.disabled}`,
      },
      '& .react-time-picker__inputGroup__input': {
        color: theme.palette.info.main,
      },
      '& .react-time-picker__inputGroup__leadingZero, & .react-time-picker__inputGroup__divider': {
        color: theme.palette.info.main,
      },
      '& .MuiSvgIcon-root': { // disable the Description icon button if description set
        display: 'none'
      }
    }
  }
});


export const GInputTimeInput = styled("div", {
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

    '& .react-time-picker': {
      alignItems: 'center',
      width: '100%',
      boxSizing: 'border-box', // Prevent padding issue with fullWidth.
      padding: '4px 0 5px',
      border: '1px solid rgba(0, 0, 0, 0.23)',
      outline: '1px solid rgb(0,0,0, 0.0)',
      borderRadius: theme.spacing(0.5),
    },

    '& .react-time-picker__wrapper': {
      border: 'unset'
    },

    '& .react-time-picker__inputGroup__input': {
      ...theme.typography.body1,
    },
    
  };
});
