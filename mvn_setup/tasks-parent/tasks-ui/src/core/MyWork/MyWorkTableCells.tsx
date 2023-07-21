import React from 'react';
import { Box, Avatar, Chip, IconButton } from '@mui/material';

import client from '@taskclient';
import { useIntl, FormattedMessage } from 'react-intl';
import AssignmentIcon from '@mui/icons-material/Assignment';

import { TasksTableCell } from './MyWorkTableCell';

interface CellProps {
  maxWidth?: string;
  row: client.TaskDescriptor;
}


const Desc: React.FC<CellProps> = ({ row, maxWidth }) => {
  const { pallette } = client.useTasks();
  return (<Box>
    <TasksTableCell id={row.id + "/Desc"} name={row.description} maxWidth={maxWidth} />
    <Box>
      {row.entry.extensions.filter(ext => ext.type === 'pdf' || ext.type === 'upload').map((ext => (
        <Chip
          label={ext.name}
          avatar={<AssignmentIcon sx={{ fill: pallette.mywork.upload }} />}
          variant="outlined"
        />
      )))}
    </Box>
  </Box>);
}
const DueDate: React.FC<CellProps> = ({ row }) => {
  return (<TasksTableCell id={row.id + "/DueDate"} name={row.dueDate ? row.dueDate.toISOString() : undefined} />);
}
const Subject: React.FC<CellProps> = ({ row, maxWidth }) => {
  return (<TasksTableCell id={row.id + "/Subject"} name={row.title} maxWidth={maxWidth} />);
}
const Tools: React.FC<CellProps> = ({ row, maxWidth }) => {
  const { pallette } = client.useTasks();

  //mywork.button.review
  return (<Box id={`${row.id}/Tools`} sx={{ maxWidth }}>
    <IconButton aria-label="delete">
      <AssignmentIcon sx={{ fill: pallette.mywork.review }} />
    </IconButton>
  </Box>);
}



export type { CellProps }
export { Subject, DueDate, Desc, Tools };

