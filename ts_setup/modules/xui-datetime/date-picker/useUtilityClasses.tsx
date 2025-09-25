import { generateUtilityClass, styled } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'




export const MUI_NAME = 'XuiDatePicker';


export const useUtilityClasses = () => {
  const slots = { root: ['root'] };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {
    root: [].join(' ')
  });
}


export const XuiDateFieldRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => [
    styles.root
  ],
})<{ 
  ownerState: { variant: 'classic' | 'mui-like',
  fullWidth: boolean
} }>(({ theme, ownerState }) => {
  const {variant, fullWidth} = ownerState;

  return {
    width: fullWidth ? '100%' : (variant === 'classic' ? 'fit-content' : '100%')
  };
});