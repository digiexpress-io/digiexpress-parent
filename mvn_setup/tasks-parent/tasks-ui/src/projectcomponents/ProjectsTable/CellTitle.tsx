import React from 'react';
import { Box } from '@mui/material';

import Burger from '@the-wrench-io/react-burger';

import { ProjectDescriptor, Group } from 'projectdescriptor';
import Context from 'context';

import TaskCell from './TaskCell';
import { StyledTableCell } from './StyledTable';



const FormattedCell: React.FC<{
  rowId: number,
  row: ProjectDescriptor,
  def: Group,
}> = ({ row }) => {

  const apps = Burger.useApps();
  const projectId = Context.useProjectId();

  function handleOpenProject() {
    projectId.setProjectId(row.repoId);
    apps.actions.handleActive(row.appId);
  }

  return (
    <StyledTableCell width="500px" onClick={handleOpenProject}>
      <Box justifyContent='left' display='flex'>
        <TaskCell id={row.id + "/Subject"} name={row.title} maxWidth={"500px"} />
      </Box>
    </StyledTableCell>
  );

}

export default FormattedCell;

