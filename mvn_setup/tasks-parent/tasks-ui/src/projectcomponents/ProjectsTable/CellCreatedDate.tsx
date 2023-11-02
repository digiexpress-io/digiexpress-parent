import React from 'react';
import { Dialog, IconButton, Box, styled } from '@mui/material';
import DateRangeOutlinedIcon from '@mui/icons-material/DateRangeOutlined';
import TimestampFormatter from 'timestamp';
import { ProjectDescriptor, Group } from 'projectdescriptor';
import TaskCell from './TaskCell';
import { CellProps } from './task-table-types';
import { StyledTableCell } from './StyledTable';

const StyledDateRangeOutlinedIcon = styled(DateRangeOutlinedIcon)(({ theme }) => ({
  fontSize: 'medium',
  color: theme.palette.uiElements.main
}));

const FormattedCell: React.FC<{
  rowId: number,
  row: ProjectDescriptor,
  def: Group
}> = ({ row, def }) => {

  return (<StyledTableCell width='180px'>
    <Box width='180px'>
      <TimestampFormatter value={row.created} type='date' />
    </Box>
  </StyledTableCell>);
}

export default FormattedCell;
