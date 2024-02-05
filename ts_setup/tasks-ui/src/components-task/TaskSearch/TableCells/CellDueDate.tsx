import React from 'react';
import { Box } from '@mui/material';

import { TaskDescriptor, ChangeTaskDueDate } from 'descriptor-task';
import { StyledTableCell } from 'components-generic';

import Context from 'context';

import TaskCell from './TaskCell';
import TaskDueDate from '../../TaskDueDate';


const FormattedCell: React.FC<{
  rowId: number,
  row: TaskDescriptor
}> = ({ row }) => {

  const tasks = Context.useTasks();

  async function handleDueDateChange(dueDate: string | undefined) {
    const command: ChangeTaskDueDate = {
      commandType: 'ChangeTaskDueDate',
      dueDate,
      taskId: row.id
    };

    await tasks.updateActiveTask(row.id, [command]);
  }


  return (<StyledTableCell width='180px'>
    <Box width='180px'>
      <TaskCell id={row.id + "/DueDate"} name={<TaskDueDate task={row} onChange={handleDueDateChange} />} />
    </Box>
  </StyledTableCell>);
}

export default FormattedCell;
