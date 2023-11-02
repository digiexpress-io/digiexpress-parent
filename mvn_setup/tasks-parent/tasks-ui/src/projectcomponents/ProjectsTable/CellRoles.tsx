import React from 'react';

import { StyledTableCell } from './StyledTable';
import TaskRoles from 'taskcomponents/TaskRoles';
import Context from 'context';
import { TaskDescriptor, Group } from 'taskdescriptor';
import Client from 'client';

const FormattedCell: React.FC<{
  rowId: number,
  row: TaskDescriptor,
  def: Group
}> = ({ row }) => {

  const tasks = Context.useTasks();
  const backend = Context.useBackend();

  async function handleChange(command: Client.AssignTaskRoles) {
    await backend.task.updateActiveTask(row.id, [command]);
    await tasks.reload();
  }

  return (
    <StyledTableCell><TaskRoles task={row} onChange={handleChange} /></StyledTableCell>
  );
}

export default FormattedCell;


