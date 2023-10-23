import React from 'react';

import Client from '@taskclient';
import Context from 'context';
import { StyledTableCell } from './StyledTable';
import TaskAssignees from 'core/TaskAssignees';

const FormattedCell: React.FC<{
  rowId: number,
  row: Context.TaskDescriptor,
}> = ({ row }) => {

  const tasks = Context.useTasks();
  const backend = Context.useBackend();

  async function handleChange(command: Client.AssignTask) {
    await backend.task.updateActiveTask(row.id, [command]);
    await tasks.reload();
  }

  return (
    <StyledTableCell width="150px">
      <TaskAssignees task={row} onChange={handleChange} />
    </StyledTableCell>
  );
}

export default FormattedCell;