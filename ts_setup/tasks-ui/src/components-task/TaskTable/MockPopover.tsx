import React from 'react';
import { Box, Popover, PopoverOrigin } from '@mui/material';


interface TablePopoverProps {
  open: boolean,
  onClose: () => void,
  children: React.ReactNode,
  anchorEl: HTMLElement | null,
  anchorOrigin?: PopoverOrigin,
  transformOrigin?: PopoverOrigin
}

const useMockPopover = () => {
  const [anchorEl, setAnchorEl] = React.useState<HTMLElement | null>(null);
  const open = Boolean(anchorEl);

  const handleClick = React.useCallback((event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  }, [setAnchorEl]);

  const handleClose = React.useCallback(() => {
    setAnchorEl(null);
  }, [setAnchorEl]);


  const Delegate: React.FC<{ children: React.ReactNode, onClose: () => void }> = React.useCallback(({ children, onClose }) => {
    return (<TablePopover open={open} anchorEl={anchorEl} onClose={onClose}>
      {children}
    </TablePopover>)
  }, [open, anchorEl])

  return { Delegate, onClick: handleClick, onClose: handleClose };
}

const TablePopover: React.FC<TablePopoverProps> = ({ children, anchorEl, open, onClose }) => {

  return (
    <Popover open={open} onClose={onClose} anchorEl={anchorEl}
      anchorOrigin={{
        vertical: 'bottom',
        horizontal: 'right',
      }}
      transformOrigin={{
        vertical: 'top',
        horizontal: 'right',
      }}>
      <Box sx={{ p: 0, minHeight: '20vh' }}>
        {children}
      </Box>

    </Popover>
  );
}

export { useMockPopover };