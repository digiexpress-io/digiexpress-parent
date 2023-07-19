import React from 'react';
import { SxProps, Box, Typography } from '@mui/material';
import { StyledButton } from './StyledButton';


const textStyle: SxProps = {
  color: 'text.primary',
  fontWeight: 'bold',
  textAlign: 'left'
}

const buttonClickStyle: SxProps = {
  width: '100%',
  justifyContent: 'start'
}

const buttonNotClickStyle: SxProps = {
  width: '100%',
  justifyContent: 'start',
  pointerEvents: 'none'
}


interface StyledLinkProps {
  startIcon: React.ReactElement,
  name: string,
  onClick?: () => void;
}

const StyledLink: React.FC<StyledLinkProps> = ({ startIcon, onClick, name }) => {
  const buttonStyle = onClick ? buttonClickStyle : buttonNotClickStyle;
  return (
    <Box display='flex'>
      <StyledButton sx={buttonStyle} startIcon={startIcon} variant='text'>
        <Typography sx={textStyle} onClick={onClick}>{name}</Typography>
      </StyledButton>
    </Box>)
}

export type { StyledLinkProps }
export { StyledLink }