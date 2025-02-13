import React from 'react';
import { Box, Popover } from '@mui/material';


interface TablePopoverProps {
  open: boolean,
  onClose: () => void,
  children: React.ReactNode,
  anchorEl: HTMLElement | null
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
      <Box sx={{ p: 0, minHeight: '10vh' }}>
        {children}
      </Box>

    </Popover>
  );
}

const usePopover = () => {
  const [anchorEl, setAnchorEl] = React.useState<HTMLElement | null>(null);
  const open = Boolean(anchorEl);

  const handleClick = React.useCallback((event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  }, [setAnchorEl]);

  const handleClose = React.useCallback(() => {
    setAnchorEl(null);
  }, [setAnchorEl]);


  return { onClick: handleClick, onClose: handleClose, anchorEl, open };
}

export type { TablePopoverProps }
export { usePopover, TablePopover };