import React from 'react';

import Client from '@taskclient';

import { StyledTableCell } from './StyledTable';
import TaskStatuses from 'core/TaskStatuses';

const FormattedCell: React.FC<{
  rowId: number,
  row: Client.TaskDescriptor,
  def: Client.Group
}> = ({ row }) => {

  return (
    <StyledTableCell width="100px" sx={{ pl: 0 }}><TaskStatuses task={row} /></StyledTableCell>
  );
}

export default FormattedCell;

