import React from 'react';
import { Box } from '@mui/material';

import { CustomerDescriptor } from 'descriptor-customer';
import { StyledTableCell } from 'components-generic';

import TableCell from './TableCell';


const FormattedCell: React.FC<{
  rowId: number;
  row: CustomerDescriptor;
  children: React.ReactNode;
}> = ({ row, children }) => {

  return (
    <StyledTableCell width="500px">
      <Box justifyContent='left' display='flex'>
        <TableCell id={row.id + "/DisplayName"} name={row.displayName} maxWidth={"500px"} />
        {children}
      </Box>
    </StyledTableCell>
  );

}

export default FormattedCell;

