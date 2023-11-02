import React from 'react';

import Context from 'context';
import { ProjectDescriptor, Group } from 'projectdescriptor';
import Client from 'client';
import { StyledTableCell } from './StyledTable';
import ProjectRepoType from '../ProjectRepoType';

const FormattedCell: React.FC<{
  rowId: number,
  row: ProjectDescriptor,
  def: Group
}> = ({ row }) => {
  const tasks = Context.useProjects();
  const backend = Context.useBackend();

  async function handleChange(command: Client.ChangeRepoType) {
    await backend.project.updateActiveProject(row.id, [command]);
    await tasks.reload();
  }


  return (<StyledTableCell width="50px" ><ProjectRepoType task={row} onChange={handleChange} /></StyledTableCell>);
}

export default FormattedCell;

