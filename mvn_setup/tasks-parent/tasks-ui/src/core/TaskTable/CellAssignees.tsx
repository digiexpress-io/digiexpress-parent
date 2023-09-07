import React from 'react';

import Client from '@taskclient';
import { StyledTableCell } from './StyledTable';
import TaskAssignees from 'core/TaskAssignees';

const FormattedCell: React.FC<{
  rowId: number,
  row: Client.TaskDescriptor,
}> = ({ row }) => {

  return (
    <StyledTableCell width="150px">
      <TaskAssignees task={row}/>
    </StyledTableCell>
  );
}

export default FormattedCell;