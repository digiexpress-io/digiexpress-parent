import React from 'react';
import { Box } from '@mui/material';
import { TaskDescriptor, Group } from 'descriptor-task';
import TaskCell from './TaskCell';
import { StyledTableCell } from './StyledTable';
import TaskDueDate from '../TaskDueDate';
import Context from 'context';
import Client from 'client';

const FormattedCell: React.FC<{
  rowId: number,
  row: TaskDescriptor,
  def: Group
}> = ({ row, def }) => {

  const backend = Context.useBackend();
  const tasks = Context.useTasks();

  async function handleDueDateChange(dueDate: string | undefined) {
    const command: Client.ChangeTaskDueDate = {
      commandType: 'ChangeTaskDueDate',
      dueDate,
      taskId: row.id
    };

    await backend.task.updateActiveTask(row.id, [command]);
    await tasks.reload();
  }


  return (<StyledTableCell width='180px'>
    <Box width='180px'>
      <TaskCell id={row.id + "/DueDate"} name={<TaskDueDate task={row} onChange={handleDueDateChange} />} />
    </Box>
  </StyledTableCell>);
}

export default FormattedCell;
