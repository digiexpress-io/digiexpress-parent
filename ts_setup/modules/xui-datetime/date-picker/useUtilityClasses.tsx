import { generateUtilityClass, styled } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'

export const MUI_NAME = 'XuiDatePicker';


export const useUtilityClasses = () => {
  const slots = { root: ['root'], input: ['input'] };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

export const XuiDateFieldRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => [
    styles.root
  ],
})<{ 
  ownerState: { fullWidth: boolean }
 }>(({ ownerState }) => {
  const {fullWidth} = ownerState;

  return {
    width: fullWidth ? '100%' : 'fit-content'
  };
});

export const XuiDateFieldInput = styled('div', {
  name: MUI_NAME,
  slot: 'Input',
  overridesResolver: (props, styles) => [
    styles.root
  ],
})<{ 
  ownerState: { 
    isError: boolean,
    size: 'small' | 'medium'
} }>(({ theme, ownerState }) => {
  
  const { isError, size } = ownerState;
  const height = size === 'small' ? 40 : 56;

  return {
    height,
    border: `1px solid ${theme.palette.action.disabled}`,

    ...(isError && { border: `1px solid ${theme.palette.error.main}` }),
    '&:focus-within': !isError ? {
      border: `1px solid ${theme.palette.primary.main}`,
      boxShadow: `0 0 0 1px ${theme.palette.primary.main}`
    } : undefined,
    borderRadius: theme.shape.borderRadius,
    
    paddingLeft: theme.spacing(1),
    paddingRight: theme.spacing(1),

    width: '100%',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
  };
});