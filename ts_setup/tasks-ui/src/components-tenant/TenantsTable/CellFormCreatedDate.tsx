import React from 'react';
import { Box } from '@mui/material';
import { TenantEntryDescriptor, Group } from 'descriptor-tenant';
import { StyledTableCell } from './StyledTable';
import Burger from 'components-burger';

const FormattedCell: React.FC<{
  rowId: number,
  row: TenantEntryDescriptor,
  def: Group
}> = ({ row }) => {

  return (<StyledTableCell width='180px'>
    <Box width='180px'>
      <Burger.DateTimeFormatter value={row.created} type='date' />
    </Box>
  </StyledTableCell>);
}

export default FormattedCell;
