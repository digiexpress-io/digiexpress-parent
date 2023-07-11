import React from 'react';
import { Dialog, DialogContent } from '@mui/material';
import { useTable } from '../descriptor-table-ctx';


const StyledPopper: React.FC<{ id: string, content: React.ReactNode }> = ({ id, content }) => {
  const { state, setState } = useTable();
  const { popperOpen, popperId } = state;
  const open = popperOpen && popperId === id;
  const handleClose = React.useCallback((_event: React.MouseEvent<HTMLButtonElement>) => {
    setState(prev => prev.withPopperOpen(id, false))
  }, [setState, id]);

  if (popperId !== id) {
    return null;
  }

  return (<Dialog open={open} onClose={handleClose} maxWidth="md">
    <DialogContent sx={{padding: 'unset'}}>{content}</DialogContent>
  </Dialog>);
}

const Info: React.FC<{ id: string, content: React.ReactNode }> = ({ id, content }) => {
  return (<><StyledPopper id={id} content={content} /></>);
}

export default Info;

