import React from 'react';

import Client from '@taskclient';
import Context from 'context';

import { StyledTableCell } from './StyledTable';
import TaskStatus from 'core/TaskStatus';

const FormattedCell: React.FC<{
  rowId: number,
  row: Context.TaskDescriptor,
  def: Context.Group,
}> = ({ row }) => {
  const tasks = Context.useTasks();
  const backend = Context.useBackend();

  async function handleChange(command: Client.ChangeTaskStatus) {
    await backend.task.updateActiveTask(row.id, [command]);
    await tasks.reload();
  }


  return (
    <StyledTableCell width="100px" sx={{ pl: 0 }}><TaskStatus task={row} onChange={handleChange} /></StyledTableCell>
  );
}

export default FormattedCell;

