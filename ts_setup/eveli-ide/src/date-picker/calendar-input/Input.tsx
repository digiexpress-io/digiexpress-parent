import { styled} from '@mui/material';


export const Input = styled('input')(({ theme }) => ({

  fontFamily: 'inherit',
  fontSize: 'inherit',
  color: 'inherit',
  padding: 'unset' ,
  border: 'unset',
  
  // Reset all browser defaults
  appearance: 'none',
  WebkitAppearance: 'none',
  MozAppearance: 'none',
  margin: 0,
    
  // Remove default styling
  outline: 'none',
  background: 'transparent',
  
  // Remove default font styling
  fontWeight: 'inherit',
  lineHeight: 'inherit',
  
  // Remove other browser quirks
  boxSizing: 'border-box',
  width: 'auto',
  height: 'auto',

  // Remove focus outline
  '&:focus': {
    outline: 'none',
    boxShadow: 'none',
  },
  
  // Remove autofill styling
  '&:-webkit-autofill': {
    WebkitBoxShadow: '0 0 0 1000px transparent inset',
    WebkitTextFillColor: 'inherit',
  }
}));