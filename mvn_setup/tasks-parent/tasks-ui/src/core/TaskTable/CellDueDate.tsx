import React from 'react';
import { Dialog, IconButton, Box, styled } from '@mui/material';
import DateRangeOutlinedIcon from '@mui/icons-material/DateRangeOutlined';

import client from '@taskclient';

import TaskCell from './TaskCell';
import { CellProps } from './task-table-types';
import DatePicker from '../DatePicker';
import { StyledTableCell } from './StyledTable';

const StyledDateRangeOutlinedIcon = styled(DateRangeOutlinedIcon)(({ theme }) => ({
  fontSize: 'medium',
  color: theme.palette.uiElements.main
}));

const DueDate: React.FC<CellProps> = ({ row }) => {
  const [datePickerOpen, setDatePickerOpen] = React.useState(false);
  const [startDate, setStartDate] = React.useState<Date | string | undefined>(row.startDate);
  const [dueDate, setDueDate] = React.useState<Date | string | undefined>(row.dueDate);

  const name = row.dueDate ? 
    row.dueDate.toISOString() : 
    <IconButton onClick={() => setDatePickerOpen(true)} color='inherit'>
      <StyledDateRangeOutlinedIcon />
    </IconButton>;

  return (<>
    <Dialog open={datePickerOpen} onClose={() => setDatePickerOpen(false)}>
      <DatePicker startDate={startDate} setStartDate={setStartDate} dueDate={dueDate} setDueDate={setDueDate} />
    </Dialog>
    <TaskCell id={row.id + "/DueDate"} name={name} />
  </>
  );
}


const FormattedCell: React.FC<{
  rowId: number,
  row: client.TaskDescriptor,
  def: client.Group
}> = ({ row, def }) => {

  return (<StyledTableCell width='180px'><Box width='180px'><DueDate row={row} def={def}/></Box></StyledTableCell>);
}

export default FormattedCell;
