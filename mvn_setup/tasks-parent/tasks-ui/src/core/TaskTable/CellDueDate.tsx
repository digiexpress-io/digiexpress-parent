import React from 'react';
import { Dialog, IconButton } from '@mui/material';
import DateRangeOutlinedIcon from '@mui/icons-material/DateRangeOutlined';

import client from '@taskclient';
import Styles from '@styles';

import TaskCell from './TaskCell';
import { usePopover } from './CellPopover';
import { CellProps } from './task-table-types';
import DatePicker from '../DatePicker';


const DueDate: React.FC<CellProps> = ({ row }) => {

  const [datePickerOpen, setDatePickerOpen] = React.useState(false);
  const [startDate, setStartDate] = React.useState<Date | string | undefined>();
  const [endDate, setEndDate] = React.useState<Date | string | undefined>();

  const name = <IconButton onClick={() => setDatePickerOpen(true)} color='inherit'><DateRangeOutlinedIcon sx={{ fontSize: 'small' }} /></IconButton>;

  return (<>
    <Dialog open={datePickerOpen} onClose={() => setDatePickerOpen(false)}>
      <DatePicker startDate={startDate} setStartDate={setStartDate} endDate={endDate} setEndDate={setEndDate} />
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

  return (<Styles.TableCell><DueDate row={row} def={def}/></Styles.TableCell>);
}

export default FormattedCell;
