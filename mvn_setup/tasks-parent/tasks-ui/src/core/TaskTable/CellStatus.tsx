import React from 'react';

import Client from '@taskclient';

import { StyledTableCell } from './StyledTable';
import TaskStatus from 'core/TaskStatus';

const FormattedCell: React.FC<{
  rowId: number,
  row: Client.TaskDescriptor,
  def: Client.Group,
}> = ({ row }) => {
  const tasks = Client.useTasks();
  const backend = Client.useBackend();

  async function handleChange(command: Client.ChangeTaskStatus) {
    await backend.task.updateActiveTask(row.id, [command]);
    await tasks.reload();
  }


  return (
    <StyledTableCell width="100px" sx={{ pl: 0 }}><TaskStatus task={row} onChange={handleChange} /></StyledTableCell>
  );
}

export default FormattedCell;

