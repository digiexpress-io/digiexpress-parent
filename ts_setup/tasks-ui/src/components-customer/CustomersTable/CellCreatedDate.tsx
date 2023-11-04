import React from 'react';
import { Box } from '@mui/material';
import { ProjectDescriptor, Group } from 'descriptor-project';
import { StyledTableCell } from './StyledTable';
import Burger from 'components-burger';

const FormattedCell: React.FC<{
  rowId: number,
  row: ProjectDescriptor,
  def: Group
}> = ({ row }) => {

  return (<StyledTableCell width='180px'>
    <Box width='180px'>
      <Burger.DateTimeFormatter value={row.created} type='date' />
    </Box>
  </StyledTableCell>);
}

export default FormattedCell;
