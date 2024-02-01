import React from 'react';
import { Box } from '@mui/material';

import { CustomerDescriptor } from 'descriptor-customer';
import { StyledTableCell } from 'components-generic';


import TableCell from './TableCell';


const FormattedCell: React.FC<{
  rowId: number,
  row: CustomerDescriptor
}> = ({ row }) => {

  return (
    <StyledTableCell width="500px">
      <Box justifyContent='left' display='flex'>
        <TableCell id={row.id + "/Tasks"} name={'TODO::'} maxWidth={"500px"} />
      </Box>
    </StyledTableCell>
  );

}

export default FormattedCell;

