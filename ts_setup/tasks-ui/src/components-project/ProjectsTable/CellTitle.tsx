import React from 'react';
import { Box } from '@mui/material';

import { ProjectDescriptor, Group } from 'descriptor-project';
import Context from 'context';

import TaskCell from './TaskCell';
import { StyledTableCell } from './StyledTable';



const FormattedCell: React.FC<{
  rowId: number,
  row: ProjectDescriptor,
  def: Group,
}> = ({ row }) => {


  const apps = Context.useApp();

  function handleOpenProject() {
    apps.changeApp(row.repoType);
  }

  return (
    <StyledTableCell width="500px" onClick={handleOpenProject}>
      <Box justifyContent='left' display='flex' sx={{ cursor: 'pointer' }}>
        <TaskCell id={row.id + "/Subject"} name={row.title} maxWidth={"500px"} />
      </Box>
    </StyledTableCell>
  );

}

export default FormattedCell;

