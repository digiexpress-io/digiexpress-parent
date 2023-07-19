import React from 'react';

import { Button, Box, CssBaseline } from '@mui/material';
import { SxProps } from '@mui/system';

interface StyledButtonProps {
  onClick?: (event: React.MouseEvent<HTMLButtonElement>) => void,
  startIcon?: React.ReactElement,
  endIcon?: React.ReactElement,
  variant?: 'outlined' | 'contained' | 'text',
  disabled?: boolean,
  fullWidth?: boolean,
  sx?: SxProps,
}


const StyledButton: React.FC<StyledButtonProps> = ({ onClick, children, startIcon, variant, sx, fullWidth, endIcon }) => {

  return (
    <>
      <CssBaseline />
      <Button role='button' variant={variant} startIcon={startIcon} endIcon={endIcon} onClick={onClick} sx={{ ...sx, justifyContent: 'start' }} fullWidth={fullWidth}>
        <Box display='flex' sx={{ textAlign: 'left', textTransform: 'capitalize' }}>{children}</Box>
      </Button>
    </>
  )
}

export { StyledButton }
export type { StyledButtonProps }
