import React from 'react';


import Context from 'context';

import { TaskDescriptor, ChangeTaskStatus } from 'descriptor-task';
import { StyledTableCell } from 'components-generic';

import TaskStatus from '../../TaskStatus';


const FormattedCell: React.FC<{
  rowId: number,
  row: TaskDescriptor
}> = ({ row }) => {
  const tasks = Context.useTasks();

  async function handleChange(command: ChangeTaskStatus) {
    await tasks.updateActiveTask(row.id, [command]);
  }


  return (
    <StyledTableCell width="100px" sx={{ pl: 0 }}><TaskStatus task={row} onChange={handleChange} /></StyledTableCell>
  );
}

export default FormattedCell;

