import React from 'react';
import { Box, Avatar, AvatarGroup, IconButton, Dialog, Stack } from '@mui/material';
import DateRangeOutlinedIcon from '@mui/icons-material/DateRangeOutlined';
import AssistantPhotoTwoToneIcon from '@mui/icons-material/AssistantPhotoTwoTone';
import { useIntl } from 'react-intl';

import { DatePicker } from '../DatePicker/DatePicker';
import client from '@taskclient';

import { TasksTableCell } from './TasksTableCell';

interface CellProps {
  row: client.TaskDescriptor;
  def: client.Group;
}


const AssignPerson: React.FC = () => {
  return (
    <Stack direction="row">
      <Avatar sx={{
        width: 24,
        height: 24,
        fontSize: 10,
        ':hover': {
          cursor: 'pointer'
        }
      }} />
    </Stack>
  );
}

const Assignees: React.FC<CellProps> = ({ row, def }) => {
  const { state } = client.useTasks();
  const avatars = row.assigneesAvatars.map((entry, index) => {
    return (
      <Avatar key={index} sx={{
        bgcolor: state.pallette.owners[entry.value],
        width: 24,
        height: 24,
        fontSize: 10,
        ':hover': {
          cursor: 'pointer'
        }
      }}>{entry.twoletters}</Avatar>
    );
  });
  return (<TasksTableCell id={row.id + "/Assignees"} name={<Box flexDirection="row" display="flex">
    {avatars.length ? <AvatarGroup spacing='small'>{avatars}</AvatarGroup> : <AssignPerson />}
  </Box>} />);
}

const Desc: React.FC<CellProps> = ({ row }) => {
  return (<TasksTableCell id={row.id + "/Desc"} name={row.description} />);
}
const DueDate: React.FC<CellProps> = ({ row }) => {

  const [datePickerOpen, setDatePickerOpen] = React.useState(false);
  const [startDate, setStartDate] = React.useState<Date | string | undefined>();
  const [endDate, setEndDate] = React.useState<Date | string | undefined>();

  return (<>
    <Dialog open={datePickerOpen} onClose={() => setDatePickerOpen(false)}>
      <DatePicker startDate={startDate} setStartDate={setStartDate} endDate={endDate} setEndDate={setEndDate} />
    </Dialog>
    <TasksTableCell id={row.id + "/DueDate"} name={
      <IconButton onClick={() => setDatePickerOpen(true)} color='inherit'><DateRangeOutlinedIcon sx={{ fontSize: 'small' }} /></IconButton>} />
  </>
  );
}
const Status: React.FC<CellProps> = ({ row }) => {
  const intl = useIntl();
  const value = intl.formatMessage({ id: `tasktable.header.spotlight.status.${row.status}` }).toUpperCase();
  return (<TasksTableCell id={row.id + "/Status"} name={value} />);
}

const Priority: React.FC<CellProps & { color?: string }> = ({ row, color }) => {
  const intl = useIntl();
  const value = intl.formatMessage({ id: `tasktable.header.spotlight.priority.${row.priority}` }).toUpperCase();
  return (<TasksTableCell id={row.id + "/Priority"} name={<IconButton><AssistantPhotoTwoToneIcon sx={{ fontSize: 'medium', color }} /></IconButton>} />);
}


const Menu: React.FC<CellProps> = ({ row }) => {
  return (<TasksTableCell id={row.id + "/Menu"} name={<></>} />);
}

const Subject: React.FC<CellProps & { maxWidth: string }> = ({ row, maxWidth }) => {
  return (<TasksTableCell id={row.id + "/Subject"} name={row.title} maxWidth={maxWidth} />);
}

export type { CellProps }
export { Subject, Priority, Status, Assignees, DueDate, Desc, Menu };

