import React from 'react';
import { Box } from '@mui/material';
import { UserProfileDescriptor } from 'descriptor-access-mgmt';
import { StyledTableCell } from 'components-generic';
import TableCell from './TableCell';

const FormattedCell: React.FC<{
  rowId: number,
  row: UserProfileDescriptor,
}> = ({ row }) => {

  return (<StyledTableCell width='180px' rowtype='medium'>
    <Box width='180px'>
      <TableCell id={row.id + "/Email"} name={(
        <Box display='flex' alignItems='center'>
          {row.email}
        </Box>)} />
    </Box>
  </StyledTableCell>);
}

export default FormattedCell;
