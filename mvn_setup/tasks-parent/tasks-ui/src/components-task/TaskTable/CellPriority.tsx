import React from 'react';

import Context from 'context';
import { TaskDescriptor, Group } from 'taskdescriptor';
import Client from 'client';
import { StyledTableCell } from './StyledTable';
import TaskPriority from '../TaskPriority';

const FormattedCell: React.FC<{
  rowId: number,
  row: TaskDescriptor,
  def: Group
}> = ({ row }) => {
  const tasks = Context.useTasks();
  const backend = Context.useBackend();

  async function handleChange(command: Client.ChangeTaskPriority) {
    await backend.task.updateActiveTask(row.id, [command]);
    await tasks.reload();
  }


  return (<StyledTableCell width="50px" ><TaskPriority task={row} onChange={handleChange} /></StyledTableCell>);
}

export default FormattedCell;

