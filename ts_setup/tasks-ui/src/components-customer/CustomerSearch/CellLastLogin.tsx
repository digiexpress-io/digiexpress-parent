import React from 'react';
import { Box } from '@mui/material';
import Burger from 'components-burger';
import { CustomerDescriptor } from 'descriptor-customer';
import { StyledTableCell } from 'components-generic';

import { CustomersSearchState } from './table-ctx'
import TableCell from './TableCell';

const FormattedCell: React.FC<{
  rowId: number,
  row: CustomerDescriptor,
  def: CustomersSearchState
}> = ({ row, def }) => {

  return (<StyledTableCell width='180px'>
    <Box width='180px'>
      <TableCell id={row.id + "/LastLogin"} name={(
        <Box display='flex' alignItems='center'>
          <Burger.DateTimeFormatter value={row.lastLogin} type='dateTime' />
        </Box>)} />
    </Box>
  </StyledTableCell>);
}

export default FormattedCell;
