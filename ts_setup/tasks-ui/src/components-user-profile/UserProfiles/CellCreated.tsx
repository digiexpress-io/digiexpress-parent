import React from 'react';
import { Box } from '@mui/material';
import Burger from 'components-burger';
import { UserProfileDescriptor } from 'descriptor-user-profile';
import { StyledTableCell } from 'components-generic';
import TableCell from './TableCell';

const FormattedCell: React.FC<{
  rowId: number,
  row: UserProfileDescriptor,
}> = ({ row }) => {

  return (<StyledTableCell width='180px'>
    <Box width='180px'>
      <TableCell id={row.id + "/Created"} name={(
        <Box display='flex' alignItems='center'>
          <Burger.DateTimeFormatter value={row.created} type='dateTime' />
        </Box>)} />
    </Box>
  </StyledTableCell>);
}

export default FormattedCell;
