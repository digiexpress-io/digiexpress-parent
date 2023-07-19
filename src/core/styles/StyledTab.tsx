import React from 'react';
import { Typography, SxProps, useTheme } from '@mui/material';
import { StyledButton } from './StyledButton';


const activeStyle: SxProps = {
  color: 'text.primary',
}

const inactiveStyle: SxProps = {
  color: 'text.primary',
}

interface StyledTabProps {
  onClick?: () => void,
  active: boolean,
  sx?: SxProps 
}


const StyledTab: React.FC<StyledTabProps> = ({ onClick, children, active, sx }) => {
  const theme = useTheme();  
  const baseStyle: SxProps = { fontWeight: theme.typography.h1.fontWeight, textTransform: 'uppercase' };
  const styles: SxProps = sx ? {...baseStyle, ...sx } : baseStyle;
  
  return (<StyledButton variant='text' sx={active ? activeStyle : inactiveStyle} onClick={onClick} fullWidth>
      <Typography sx={styles}>{children}</Typography>
    </StyledButton>);
}

export { StyledTab }
export type { StyledTabProps }