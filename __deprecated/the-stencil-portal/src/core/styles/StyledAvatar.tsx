import React from 'react';

import { alpha, Avatar, SxProps } from '@mui/material';
import { styled } from "@mui/material/styles";


const AvatarStyledSecondary = styled(Avatar)`
  ${({ theme }) => `
    color: ${theme.palette.text.primary}; 
    background-color: ${alpha(theme.palette.secondary.main, 0.1)}; 
    border: 3px solid ${alpha(theme.palette.secondary.main, 0.8)};
  `};
`;

const AvatarStyledPrimary = styled(Avatar)` 
  ${({ theme }) => `
    color: ${theme.palette.primary.main};
    background-color: unset; 
  `};
`;


interface StyledAvatarProps {
  children: string | number | React.ReactElement, 
  sx?: SxProps,
  variant?: 'primary' | 'secondary', 
  onClick?:() => void;
}


const StyledAvatar: React.FC<StyledAvatarProps> = ({ children, sx, onClick, variant }) => {
  if(variant === 'secondary') {
    return (<AvatarStyledSecondary sx={sx} onClick={onClick}>{children}</AvatarStyledSecondary>)  
  }
  
  return (<AvatarStyledPrimary sx={sx} onClick={onClick}>{children}</AvatarStyledPrimary>)
}


export { StyledAvatar }
export type { StyledAvatarProps }

