import React from 'react';

import Client from '@taskclient';
import { StyledTableCell } from './StyledTable';
import TaskPriority from 'core/TaskPriority';

const FormattedCell: React.FC<{
  rowId: number,
  row: Client.TaskDescriptor,
  def: Client.Group
}> = ({ row }) => {

  return (<StyledTableCell width="50px" ><TaskPriority task={row} /></StyledTableCell>);
}

export default FormattedCell;

