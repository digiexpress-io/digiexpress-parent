import React from 'react';

import { TaskDescriptor, AssignTaskRoles, useTasks } from 'descriptor-task';
import { StyledTableCell } from 'components-generic';

import TaskRoles from '../../TaskRoles';


const FormattedCell: React.FC<{
  rowId: number,
  row: TaskDescriptor,
}> = ({ row }) => {

  const tasks = useTasks();
  async function handleChange(command: AssignTaskRoles) {
    await tasks.updateActiveTask(row.id, [command]);
  }

  return (<StyledTableCell><TaskRoles task={row} onChange={handleChange} /></StyledTableCell>);
}

export default FormattedCell;


