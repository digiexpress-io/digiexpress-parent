import React from 'react';

import Client from '@taskclient';
import { StyledTableCell } from './StyledTable';
import TaskPriority from 'core/TaskPriority';

const FormattedCell: React.FC<{
  rowId: number,
  row: Client.TaskDescriptor,
  def: Client.Group
}> = ({ row }) => {
  const tasks = Client.useTasks();
  const backend = Client.useBackend();

  async function handleChange(command: Client.ChangeTaskPriority) {
    await backend.task.updateActiveTask(row.id, [command]);
    await tasks.reload();
  }


  return (<StyledTableCell width="50px" ><TaskPriority task={row} onChange={handleChange} /></StyledTableCell>);
}

export default FormattedCell;

