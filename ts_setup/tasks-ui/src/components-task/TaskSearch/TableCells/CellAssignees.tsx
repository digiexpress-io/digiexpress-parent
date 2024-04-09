import React from 'react';

import { TaskDescriptor, AssignTask, useTasks } from 'descriptor-task';
import { StyledTableCell } from 'components-generic';
import { PrincipalId } from 'descriptor-access-mgmt';

import TaskAssignees from '../../TaskAssignees';


const FormattedCell: React.FC<{
  rowId: number,
  row: TaskDescriptor,
}> = ({ row }) => {

  const tasks = useTasks();

  async function handleChange(assigneeIds: PrincipalId[]) {
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