import React from 'react';
import { Chip, Box, Typography, Dialog, DialogContent } from '@mui/material';
import client from '@taskclient';


const StyledPopper: React.FC<{ id: string, content: React.ReactNode }> = ({ id, content }) => {
  const { state, setState } = client.useTable();
  const { popperOpen, popperId } = state;
  const open = popperOpen && popperId === id;
  const handleClose = React.useCallback((_event: React.MouseEvent<HTMLButtonElement>) => {
    setState(prev => prev.withPopperOpen(id, false))
  }, [setState, id]);

  if (popperId !== id) {
    return null;
  }

  return (<Dialog open={open} onClose={handleClose} maxWidth="md">
    <DialogContent sx={{ padding: 'unset' }}>{content}</DialogContent>
  </Dialog>);
}

const Info: React.FC<{ id: string, content: React.ReactNode }> = ({ id, content }) => {
  return (<><StyledPopper id={id} content={content} /></>);
}


const TasksTableCell: React.FC<{ id: string, name?: React.ReactNode, tag?: string, info?: React.ReactNode, maxWidth?: string }> = ({ id, name, tag, info, maxWidth }) => {
  const { setState } = client.useTable();
  const handleClick = React.useCallback((event: React.MouseEvent<HTMLElement>) => {
    setState(prev => prev.withPopperOpen(id, !prev.popperOpen, event.currentTarget))
  }, [setState, id]);

  if (!name) {
    return <>-</> 
  }

  return (<Box display='flex'>
    <Info id={id} content={info} />
    {tag && <Box sx={{ mr: 0, minWidth: '50px', alignSelf: "center" }}>
      <Chip label={tag} color="primary" variant="outlined" size="small" onClick={handleClick} />
    </Box>}
    {typeof name === 'string' ? <Box alignSelf="center" textOverflow="ellipsis" maxWidth={maxWidth}>
      <Typography noWrap={true} fontSize="13px" fontWeight="400">{name}</Typography>
    </Box> : name}
  </Box>);
}
export { TasksTableCell };

