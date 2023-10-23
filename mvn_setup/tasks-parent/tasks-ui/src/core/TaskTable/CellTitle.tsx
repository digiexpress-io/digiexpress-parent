import React from 'react';
import { Box } from '@mui/material';

import Context from 'context';
import TaskCell from './TaskCell';
import { StyledTableCell } from './StyledTable';



const FormattedCell: React.FC<{
  rowId: number,
  row: Context.TaskDescriptor,
  def: Context.Group,
  children: React.ReactNode
}> = ({ row, children }) => {

  return (
    <StyledTableCell width="500px">
      <Box justifyContent='left' display='flex'>
        <TaskCell id={row.id + "/Subject"} name={row.title} maxWidth={"500px"} />
        {children}
      </Box>
    </StyledTableCell>
  );

}

export default FormattedCell;

