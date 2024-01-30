import React from 'react';
import { Chip, Box, Typography } from '@mui/material';

import { useTaskPopper } from '../TableContext';


const TaskCell: React.FC<{
  id: string,
  name?: React.ReactNode,
  tag?: string,
  info?: React.ReactNode,
  maxWidth?: string
}> = ({ id, name, tag, maxWidth }) => {
  const { withPopperToggle } = useTaskPopper();

  const handleClick = React.useCallback((event: React.MouseEvent<HTMLElement>) => {
    withPopperToggle(id, event.currentTarget);
  }, [withPopperToggle, id]);

  if (!name) {
    return <>-</>
  }

  return (
    <Box display='flex'>
      {tag && <Box sx={{ mr: 0, minWidth: '50px', alignSelf: "center" }}>
        <Chip label={tag} color="primary" variant="outlined" size="small" onClick={handleClick} />
      </Box>}
      {typeof name === 'string' ? <Box alignSelf="center" textOverflow="ellipsis" maxWidth={maxWidth}>
        <Typography noWrap={true} variant='body1' fontWeight="400">{name}</Typography>
      </Box> : name}
    </Box>);
}
export default TaskCell;

