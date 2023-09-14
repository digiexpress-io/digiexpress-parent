import React from 'react';

import { StyledTableCell } from './StyledTable';
import TaskRoles from 'core/TaskRoles';

import Client from '@taskclient';

const FormattedCell: React.FC<{
  rowId: number,
  row: Client.TaskDescriptor,
  def: Client.Group
}> = ({ row }) => {

  return (
    <StyledTableCell><TaskRoles task={row} /></StyledTableCell>
  );
}

export default FormattedCell;


