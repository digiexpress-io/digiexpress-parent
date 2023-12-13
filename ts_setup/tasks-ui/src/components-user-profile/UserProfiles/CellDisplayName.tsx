import React from 'react';
import { Box } from '@mui/material';

import { UserProfileDescriptor } from 'descriptor-user-profile';
import { StyledTableCell } from 'components-generic';

import TableCell from './TableCell';


const FormattedCell: React.FC<{
  rowId: number,
  row: UserProfileDescriptor,
  children: React.ReactNode
}> = ({ row, children }) => {

  return (
    <StyledTableCell width="500px">
      <Box justifyContent='left' display='flex'>
        <TableCell id={row.id + "/Displayname"} name={row.entry.details.username} maxWidth={"500px"} />
        {children}
      </Box>
    </StyledTableCell>
  );

}

export default FormattedCell;

