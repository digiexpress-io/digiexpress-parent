import React from 'react';
import { Chip, Box, Typography } from '@mui/material';
import client from '@taskclient';


const Cell: React.FC<{
  id: string,
  name?: React.ReactNode,
  tag?: string,
  info?: React.ReactNode,
  maxWidth?: string
}> = ({ id, name, tag, maxWidth }) => {
  const { setState } = client.useTable();
  const handleClick = React.useCallback((event: React.MouseEvent<HTMLElement>) => {
    setState(prev => prev.withPopperOpen(id, !prev.popperOpen, event.currentTarget))
  }, [setState, id]);

  if (!name) {
    return <>-</>
  }

  return (
    <Box display='flex'>
      {tag && <Box sx={{ mr: 0, minWidth: '50px', alignSelf: "center" }}>
        <Chip label={tag} color="primary" variant="outlined" size="small" onClick={handleClick} />
      </Box>}
      {typeof name === 'string' ? <Box alignSelf="center" textOverflow="ellipsis" maxWidth={maxWidth}>
        <Typography noWrap={true} fontSize="13px" fontWeight="400">{name}</Typography>
      </Box> : name}
    </Box>);
}
export default Cell;
