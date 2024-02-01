import React from 'react';


import Client from 'client';
import Context from 'context';

import { TaskDescriptor } from 'descriptor-task';
import { StyledTableCell } from 'components-generic';

import TaskRoles from '../../TaskRoles';


const FormattedCell: React.FC<{
  rowId: number,
  row: TaskDescriptor,
}> = ({ row }) => {

  const tasks = Context.useTasks();
  const backend = Context.useBackend();

  async function handleChange(command: Client.AssignTaskRoles) {
    await backend.task.updateActiveTask(row.id, [command]);
    await tasks.reload();
  }

  return (<StyledTableCell><TaskRoles task={row} onChange={handleChange} /></StyledTableCell>);
}

export default FormattedCell;


