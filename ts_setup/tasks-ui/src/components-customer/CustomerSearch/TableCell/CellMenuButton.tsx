import React from 'react';
import { IconButton } from '@mui/material';
import { SxProps } from '@mui/system';
import { cyan } from 'components-colors';


const iconButtonSx: SxProps = { fontSize: 'small', color: cyan, p: 0.5 }

const CellHoverButton: React.FC<{ onClick: (event: React.MouseEvent<HTMLElement>) => void, children: React.ReactNode }> = ({ onClick, children }) => {
  return (
    <IconButton sx={iconButtonSx} onClick={onClick}>
      {children}
    </IconButton>)
}

export default CellHoverButton;

