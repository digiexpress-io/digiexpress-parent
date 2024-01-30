import React from 'react';

import Client from 'client';
import Context from 'context';

import { TaskDescriptor, Group } from 'descriptor-task';
import { StyledTableCell } from 'components-generic';

import TaskStatus from '../../TaskStatus';


const FormattedCell: React.FC<{
  rowId: number,
  row: TaskDescriptor
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

