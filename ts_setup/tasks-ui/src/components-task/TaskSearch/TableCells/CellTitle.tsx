import React from 'react';
import { Box } from '@mui/material';

import { StyledTableCell } from 'components-generic';
import { TaskDescriptor, Group } from 'descriptor-task';

import TaskCell from './TaskCell';


const CellTitle: React.FC<{
  rowId: number,
  row: TaskDescriptor
}> = ({ row }) => {

  return (
    <StyledTableCell width="500px">
      <Box justifyContent='left' display='flex'>
        <TaskCell id={row.id + "/Subject"} name={row.title} maxWidth={"500px"} />
      </Box>
    </StyledTableCell>
  );

}

export default CellTitle;

