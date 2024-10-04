import { PopoverOrigin } from '@mui/material';
import React from 'react';


export interface AnchorProps {
  anchorOrigin: PopoverOrigin;
  transformOrigin: PopoverOrigin;
  open: boolean;
  anchorEl: HTMLButtonElement | null;
  onClose: () => void;
}

export function useAnchor() {
  const [anchorEl, setAnchorEl] = React.useState<HTMLButtonElement | null>(null);

  function onClick(event: React.MouseEvent<HTMLButtonElement>) {
    setAnchorEl(event.currentTarget);
  }

  function onClose() {
    setAnchorEl(null);
  }

  const open = Boolean(anchorEl);
  const anchorProps: AnchorProps = {
    onClose,
    anchorEl,
    open,
    anchorOrigin: { horizontal: 'left', vertical: 'bottom' },
    transformOrigin: { horizontal: 'left', vertical: 'top' },
  }

  return {
    onClick, anchorProps
  }
}

