import { styled } from '@mui/material';


export const YearInput = styled('input')({
  fontFamily: 'inherit',
  fontSize: 'inherit',
  color: 'inherit',
  padding: 'unset',
  border: 'unset',
  appearance: 'none',
  WebkitAppearance: 'none',
  MozAppearance: 'none',
  margin: 0,
  outline: 'none',
  background: 'transparent',
  fontWeight: 'inherit',
  lineHeight: 'inherit',
  boxSizing: 'border-box',
  flex: 1,
  '&:focus': {
    outline: 'none',
    boxShadow: 'none',
  },
  '&:-webkit-autofill': {
    WebkitBoxShadow: '0 0 0 1000px transparent inset',
    WebkitTextFillColor: 'inherit',
  },
});
