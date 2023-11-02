import React from 'react';
import { Box } from '@mui/material';

import { ProjectDescriptor, Group } from 'projectdescriptor';

import TaskCell from './TaskCell';
import { StyledTableCell } from './StyledTable';



const FormattedCell: React.FC<{
  rowId: number,
  row: ProjectDescriptor,
  def: Group,
}> = ({ row }) => {

  return (
    <StyledTableCell width="500px">
      <Box justifyContent='left' display='flex'>
        <TaskCell id={row.id + "/Subject"} name={row.title} maxWidth={"500px"} />
      </Box>
    </StyledTableCell>
  );

}

export default FormattedCell;

