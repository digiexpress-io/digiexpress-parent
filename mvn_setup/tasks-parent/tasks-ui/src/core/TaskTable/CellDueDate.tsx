import React from 'react';
import { Dialog, IconButton, Box } from '@mui/material';
import DateRangeOutlinedIcon from '@mui/icons-material/DateRangeOutlined';

import client from '@taskclient';

import TaskCell from './TaskCell';
import { CellProps } from './task-table-types';
import DatePicker from '../DatePicker';
import { StyledTableCell } from './StyledTable';


const DueDate: React.FC<CellProps> = ({ row }) => {
  const [datePickerOpen, setDatePickerOpen] = React.useState(false);
  const [startDate, setStartDate] = React.useState<Date | string | undefined>(row.startDate);
  const [dueDate, setDueDate] = React.useState<Date | string | undefined>(row.dueDate);

  const name = <IconButton onClick={() => setDatePickerOpen(true)} color='inherit'><DateRangeOutlinedIcon sx={{ fontSize: 'small' }} /></IconButton>;

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

  return (<StyledTableCell width='70px'><Box width='70px'><DueDate row={row} def={def}/></Box></StyledTableCell>);
}

export default FormattedCell;
