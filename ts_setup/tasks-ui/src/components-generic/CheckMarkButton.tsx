import React from 'react';
import { Button } from '@mui/material';
import { FlyoutMenuItemIcon } from 'components-flyout-menu';


export interface CheckMarkButtonProps {
  onClick: (newValue: boolean, event: React.MouseEvent<HTMLElement>) => void,
  children: boolean | undefined
}

export const CheckMarkButton: React.FC<CheckMarkButtonProps> = ({ onClick, children }) => {

  function handleOnClick(event: React.MouseEvent<HTMLElement>) {
    onClick(!children, event);
  }

  return (
    <Button startIcon={<FlyoutMenuItemIcon>{children}</FlyoutMenuItemIcon>}
      sx={{ borderRadius: "20px", height: "100%"}}
      onClick={handleOnClick} 
   />
  )
}