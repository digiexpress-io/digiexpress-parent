import React from 'react';
import { Box } from '@mui/material';

import { TaskDescriptor, Group } from 'descriptor-task';
import TaskCell from './TaskCell';
import { StyledTableCell } from './StyledTable';



const CellTitle: React.FC<{
  rowId: number,
  row: TaskDescriptor,
  def: Group,
  children: React.ReactNode
}> = ({ row, children }) => {

  return (
    <StyledTableCell width="500px">
      <Box justifyContent='left' display='flex'>
        <TaskCell id={row.id + "/Subject"} name={row.title} maxWidth={"500px"} />
        <>{children}</>
      </Box>
    </StyledTableCell>
  );

}

export default CellTitle;

