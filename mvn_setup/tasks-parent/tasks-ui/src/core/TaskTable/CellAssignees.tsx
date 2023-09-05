import React from 'react';

import Client from '@taskclient';
import { StyledTableCell } from './StyledTable';
import Assignee from 'core/Assignee';

const FormattedCell: React.FC<{
  rowId: number,
  row: Client.TaskDescriptor,
}> = ({ row }) => {

  return (
    <StyledTableCell width="150px">
      <Assignee task={row}/>
    </StyledTableCell>
  );
}

export default FormattedCell;