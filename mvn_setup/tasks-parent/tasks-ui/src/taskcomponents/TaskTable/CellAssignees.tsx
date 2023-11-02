import React from 'react';

import Client from 'client';
import Context from 'context';
import { TaskDescriptor } from 'taskdescriptor';
import { StyledTableCell } from './StyledTable';
import TaskAssignees from 'taskcomponents/TaskAssignees';

const FormattedCell: React.FC<{
  rowId: number,
  row: TaskDescriptor,
}> = ({ row }) => {

  const tasks = Context.useTasks();
  const backend = Context.useBackend();

  async function handleChange(assigneeIds: Client.UserId[]) {
    const command: Client.AssignTask = { assigneeIds, commandType: 'AssignTask', taskId: row.id };
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