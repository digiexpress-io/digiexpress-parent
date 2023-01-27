import React from 'react';
import { Box, Popper, Fade, Paper, IconButton, Dialog, DialogContent } from '@mui/material';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';

import { useTable } from './descriptor-table-ctx';

const StyledPopper: React.FC<{ id: string, content: React.ReactNode }> = ({ id, content }) => {
  const { state, setState } = useTable();
  const { popperOpen, popperId } = state;
  const open = popperOpen && popperId === id;
  const handleClose = React.useCallback((event: React.MouseEvent<HTMLButtonElement>) => {
    setState(prev => prev.withPopperOpen(id, false))
  }, [setState, id]);

  if (popperId !== id) {
    return null;
  }


  return (<Dialog open={open} onClose={handleClose} maxWidth="md">
    <DialogContent sx={{padding: 'unset'}}>{content}</DialogContent>
  </Dialog>);
}


/**
 
    <Popper open={open} anchorEl={anchorEl} placement="left-end" transition
    modifiers={[{
      name: 'flip',
      enabled: true,
      options: {
        altBoundary: true,
        rootBoundary: "window",
        padding: 8,
      },
    }]}
  >
    {({ TransitionProps }) => (<Fade {...TransitionProps} timeout={350}><Box>{content}</Box></Fade>)}
  </Popper>
 */

const Info: React.FC<{ id: string, content: React.ReactNode }> = ({ id, content }) => {
  const { setState } = useTable();

  const handleClick = React.useCallback((event: React.MouseEvent<HTMLButtonElement>) => {
    setState(prev => prev.withPopperOpen(id, !prev.popperOpen, event.currentTarget))
  }, [setState, id]);

  const iconButton = React.useMemo(() => {
    return (
      <IconButton size="small" color="primary" onClick={handleClick}>
        <InfoOutlinedIcon fontSize="small" />
      </IconButton>);
  }, [handleClick]);

  return (<>{iconButton}<StyledPopper id={id} content={content} /></>);
}

export default Info;

