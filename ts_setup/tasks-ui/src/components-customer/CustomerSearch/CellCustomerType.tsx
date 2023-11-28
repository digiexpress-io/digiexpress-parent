import React from 'react';
import { Box } from '@mui/material';

import { CustomerDescriptor } from 'descriptor-customer';
import { StyledTableCell } from 'components-generic';

import { CustomersSearchState } from './table-ctx'
import TableCell from './TableCell';


const FormattedCell: React.FC<{
  rowId: number,
  row: CustomerDescriptor,
  def: CustomersSearchState
}> = ({ row }) => {

  return (
    <StyledTableCell width="500px">
      <Box justifyContent='left' display='flex'>
        <TableCell id={row.id + "/CustomerType"} name={row.customerType} maxWidth={"500px"} />
      </Box>
    </StyledTableCell>
  );

}

export default FormattedCell;

