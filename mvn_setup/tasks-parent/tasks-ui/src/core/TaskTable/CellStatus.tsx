import React from 'react';

import Client from '@taskclient';

import { StyledTableCell } from './StyledTable';
import TaskStatus from 'core/TaskStatus';

const FormattedCell: React.FC<{
  rowId: number,
  row: Client.TaskDescriptor,
  def: Client.Group
}> = ({ row }) => {



  return (
    <StyledTableCell width="100px" sx={{ pl: 0 }}><TaskStatus task={row} onChange={async (command) => {

    }} /></StyledTableCell>
  );
}

export default FormattedCell;

