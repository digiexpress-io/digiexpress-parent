
import { IconButton, styled } from '@mui/material';


export const IconButtonWithText = styled(IconButton)(({ }) => ({
 '& .MuiBadge-badge': {
    backgroundColor: 'transparent !important',
    color: 'inherit !important',
    fontSize: '0.6rem',
    minWidth: 'auto',
    height: 'auto',
    padding: 0,
    fontWeight: 'normal',
    pointerEvents: 'none',
    transition: 'none !important',
    '&:hover, &:focus, &:active': {
      backgroundColor: 'transparent !important',
      color: 'inherit !important',
      transform: 'none !important',
    },
  },
  // Ensure no hover effects on the badge
  '&:hover .MuiBadge-badge': {
    backgroundColor: 'transparent !important',
    color: 'inherit !important',
  },
}));
