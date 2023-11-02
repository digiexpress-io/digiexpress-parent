import React from 'react';
import { IconButton, SxProps } from '@mui/material';


const iconButtonSx: SxProps = { fontSize: 'small', color: 'uiElements.main', p: 0.5 }

const CellHoverButton: React.FC<{ onClick: (event: React.MouseEvent<HTMLElement>) => void, children: React.ReactNode }> = ({ onClick, children }) => {
  return (
    <IconButton sx={iconButtonSx} onClick={onClick}>
      {children}
    </IconButton>)
}

export default CellHoverButton;

