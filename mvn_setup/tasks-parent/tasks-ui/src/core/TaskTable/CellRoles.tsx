import React from 'react';

import { StyledTableCell } from './StyledTable';
import TaskRoles from 'core/TaskRoles';

import client from '@taskclient';

const FormattedCell: React.FC<{
  rowId: number,
  row: client.TaskDescriptor,
  def: client.Group
}> = ({ row }) => {

  return (
    <StyledTableCell><TaskRoles task={row} /></StyledTableCell>
  );
}

export default FormattedCell;


