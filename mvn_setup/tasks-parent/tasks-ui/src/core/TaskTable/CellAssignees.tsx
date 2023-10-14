import React from 'react';

import Client from '@taskclient';
import { StyledTableCell } from './StyledTable';
import TaskAssignees from 'core/TaskAssignees';

const FormattedCell: React.FC<{
  rowId: number,
  row: Client.TaskDescriptor,
}> = ({ row }) => {

  const tasks = Client.useTasks();
  const backend = Client.useBackend();

  async function handleChange(command: Client.AssignTask) {
    const updatedTask = await backend.task.updateActiveTask(row.id, [command]);
    await tasks.reload();
  }

  return (
    <StyledTableCell width="150px">
      <TaskAssignees task={row} onChange={handleChange} />
    </StyledTableCell>
  );
}

export default FormattedCell;