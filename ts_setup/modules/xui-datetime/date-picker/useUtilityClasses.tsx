import { generateUtilityClass, styled, CSSInterpolation } from '@mui/material'
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
  ownerState: { variant: 'classic' | 'mui-like',
  fullWidth: boolean
} }>(({ theme, ownerState }) => {
  const {variant, fullWidth} = ownerState;

  return {
    width: fullWidth ? '100%' : (variant === 'classic' ? 'fit-content' : '100%')
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
    variant: 'classic' | 'mui-like',
    isError: boolean,
    size: 'small' | 'medium'
} }>(({ theme, ownerState }) => {
  
  const {variant, isError, size} = ownerState;
  const height = size === 'small' ? 40 : 56;

  const classicStyles: CSSInterpolation = {
    height,
    border: '1px solid #ccc',
    borderRadius: 4,
    paddingLeft: 1,
    paddingRight: 1,
    width: 'fit-content' as const,
    marginInline: 'auto',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    '&:focus-within': { borderColor: 'blue' },
  };

  const muiLikeStyles: CSSInterpolation = {
    height,
    border: `1px solid ${isError ? theme.palette.error.main : 'rgba(0,0,0,0.23)'}`,
    borderRadius: theme.shape.borderRadius,
    paddingLeft: 1,
    paddingRight: 1,
    width: '100%',
    boxSizing: 'border-box' as const,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    '&:focus-within': {
      borderColor: isError ? theme.palette.error.main : theme.palette.primary.main,
      boxShadow: `0 0 0 2px ${theme.palette.action.focus}`,
    },
  };
  return variant === 'classic' ? classicStyles : muiLikeStyles;
});