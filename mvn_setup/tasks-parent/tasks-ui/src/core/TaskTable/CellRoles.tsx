import React from 'react';

import { StyledTableCell } from './StyledTable';
import TaskRoles from 'core/TaskRoles';

import Client from '@taskclient';

const FormattedCell: React.FC<{
  rowId: number,
  row: Client.TaskDescriptor,
  def: Client.Group
}> = ({ row }) => {

  const tasks = Client.useTasks();
  const backend = Client.useBackend();

  async function handleChange(command: Client.AssignTaskRoles) {
    await backend.task.updateActiveTask(row.id, [command]);
    await tasks.reload();
  }

  return (
    <StyledTableCell><TaskRoles task={row} onChange={handleChange} /></StyledTableCell>
  );
}

export default FormattedCell;


