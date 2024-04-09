import React from 'react';


import { TaskDescriptor, ChangeTaskPriority, useTasks } from 'descriptor-task';
import { StyledTableCell } from 'components-generic';

import TaskPriority from '../../TaskPriority';


const FormattedCell: React.FC<{
  rowId: number,
  row: TaskDescriptor
}> = ({ row }) => {
  const tasks = useTasks();

  async function handleChange(command: ChangeTaskPriority) {
    await tasks.updateActiveTask(row.id, [command]);
  }

  return (<StyledTableCell width="50px" ><TaskPriority task={row} onChange={handleChange} /></StyledTableCell>);
}

export default FormattedCell;

