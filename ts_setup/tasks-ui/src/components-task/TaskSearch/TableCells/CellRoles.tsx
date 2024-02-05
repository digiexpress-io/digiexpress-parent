import React from 'react';



import Context from 'context';

import { TaskDescriptor, AssignTaskRoles } from 'descriptor-task';
import { StyledTableCell } from 'components-generic';

import TaskRoles from '../../TaskRoles';


const FormattedCell: React.FC<{
  rowId: number,
  row: TaskDescriptor,
}> = ({ row }) => {

  const tasks = Context.useTasks();
  async function handleChange(command: AssignTaskRoles) {
    await tasks.updateActiveTask(row.id, [command]);
  }

  return (<StyledTableCell><TaskRoles task={row} onChange={handleChange} /></StyledTableCell>);
}

export default FormattedCell;


