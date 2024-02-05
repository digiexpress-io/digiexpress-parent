import React from 'react';

import Client from 'client';
import Context from 'context';

import { TaskDescriptor, AssignTask } from 'descriptor-task';
import { StyledTableCell } from 'components-generic';

import TaskAssignees from '../../TaskAssignees';


const FormattedCell: React.FC<{
  rowId: number,
  row: TaskDescriptor,
}> = ({ row }) => {

  const tasks = Context.useTasks();

  async function handleChange(assigneeIds: Client.UserId[]) {
    const command: AssignTask = { assigneeIds, commandType: 'AssignTask', taskId: row.id };
    await tasks.updateActiveTask(row.id, [command]);
  }
  return (
    <StyledTableCell width="150px">
      <TaskAssignees task={row} onChange={handleChange} />
    </StyledTableCell>
  );
}

export default FormattedCell;