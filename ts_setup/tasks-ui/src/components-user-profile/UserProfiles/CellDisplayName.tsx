import React from 'react';
import { Box } from '@mui/material';

import { UserProfileDescriptor } from 'descriptor-access-mgmt';
import { StyledTableCell } from 'components-generic';

import TableCell from './TableCell';


const FormattedCell: React.FC<{
  rowId: number,
  row: UserProfileDescriptor,
  children: React.ReactNode
}> = ({ row, children }) => {
  console.log(row.displayName)
  return (
    <StyledTableCell width="500px">
      <Box justifyContent='left' display='flex'>
        <TableCell id={row.id + "/Displayname"} maxWidth={"500px"} name={(
          <Box display='flex' alignItems='center'>
            {row.displayName}
          </Box>)}
        />
        {children}
      </Box>
    </StyledTableCell>
  );

}

export default FormattedCell;

