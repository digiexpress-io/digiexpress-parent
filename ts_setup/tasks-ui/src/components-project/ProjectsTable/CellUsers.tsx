import React from 'react';

import Client from 'client';
import Context from 'context';
import { ProjectDescriptor } from 'projectdescriptor';
import { StyledTableCell } from './StyledTable';
import ProjectUsers from '../ProjectUsers';

const FormattedCell: React.FC<{
  rowId: number,
  row: ProjectDescriptor,
}> = ({ row }) => {

  const projects = Context.useProjects();
  const backend = Context.useBackend();

  async function handleChange(assigneeIds: Client.UserId[]) {
    const command: Client.AssignProjectUsers = { users: assigneeIds, commandType: 'AssignProjectUsers', projectId: row.id };
    await backend.project.updateActiveProject(row.id, [command]);
    await projects.reload();
  }

  return (
    <StyledTableCell width="150px">
      <ProjectUsers task={row} onChange={handleChange} />
    </StyledTableCell>
  );
}

export default FormattedCell;