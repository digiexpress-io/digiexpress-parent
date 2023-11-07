import React from 'react';
import { Box } from '@mui/material';

import { TenantEntryDescriptor, Group } from 'descriptor-tenant';
import Context from 'context';

import TableCell from './TableCell';
import { StyledTableCell } from './StyledTable';



const FormattedCell: React.FC<{
  rowId: number,
  row: TenantEntryDescriptor,
  def: Group,
}> = ({ row }) => {

  return (
    <StyledTableCell width="500px">
      <Box justifyContent='left' display='flex' sx={{ cursor: 'pointer' }}>
        <TableCell id={row.formTitle + "/formTitle"} name={row.formTitle} maxWidth={"500px"} />
      </Box>
    </StyledTableCell>
  );

}

export default FormattedCell;

