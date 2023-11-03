import React from 'react';
import { Box } from '@mui/material';
import TimestampFormatter from 'timestamp';
import { ProjectDescriptor, Group } from 'projectdescriptor';
import { StyledTableCell } from './StyledTable';

const FormattedCell: React.FC<{
  rowId: number,
  row: ProjectDescriptor,
  def: Group
}> = ({ row }) => {

  return (<StyledTableCell width='180px'>
    <Box width='180px'>
      <TimestampFormatter value={row.created} type='date' />
    </Box>
  </StyledTableCell>);
}

export default FormattedCell;
