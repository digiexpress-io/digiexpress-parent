import React from 'react';

import Client from '@taskclient';
import { StyledTableCell } from './StyledTable';
import TaskPriorities from 'core/TaskPriorities';

const FormattedCell: React.FC<{
  rowId: number,
  row: Client.TaskDescriptor,
  def: Client.Group
}> = ({ row }) => {

  return (<StyledTableCell width="50px" ><TaskPriorities task={row} /></StyledTableCell>);
}

export default FormattedCell;

