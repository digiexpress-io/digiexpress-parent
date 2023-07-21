import React from 'react';
import { Box, Avatar } from '@mui/material';

import client from '@taskclient';
import { useIntl } from 'react-intl';

import { TasksTableCell } from './TasksTableCell';

interface CellProps {
  maxWidth?: string;
  row: client.TaskDescriptor;
  def: client.Group;
}



const Roles: React.FC<CellProps> = ({ row, def }) => {
  const { state } = client.useTasks();

  const avatars = row.rolesAvatars.map((entry, index) => <Avatar key={index} sx={{
    mr: 0.5,
    bgcolor: state.pallette.roles[entry.value],
    width: 24,
    height: 24,
    fontSize: 10
  }}>{entry.twoletters}</Avatar>);

  return (<TasksTableCell id={row.id + "/Roles"} name={<Box flexDirection="row" display="flex">{avatars}</Box>} />);
}
const Owners: React.FC<CellProps> = ({ row, def }) => {
  const { state } = client.useTasks();
  const avatars = row.assigneesAvatars.map((entry, index) => {
    return (<Avatar key={index} sx={{
      mr: 1,
      bgcolor: state.pallette.owners[entry.value],
      width: 24,
      height: 24,
      fontSize: 10
    }}>{entry.twoletters}</Avatar>);
  });
  return (<TasksTableCell id={row.id + "/Owners"} name={<Box flexDirection="row" display="flex">{avatars}</Box>} />);
}

const Desc: React.FC<CellProps> = ({ row }) => {
  return (<TasksTableCell id={row.id + "/Desc"} name={row.description} />);
}
const DueDate: React.FC<CellProps> = ({ row }) => {
  return (<TasksTableCell id={row.id + "/DueDate"} name={row.dueDate + ""} />);
}
const Status: React.FC<CellProps> = ({ row }) => {
  const intl = useIntl();
  const value = intl.formatMessage({ id: `tasktable.header.spotlight.status.${row.status}` }).toUpperCase();
  return (<TasksTableCell id={row.id + "/Status"} name={value} />);
}
const Priority: React.FC<CellProps> = ({ row }) => {
  const intl = useIntl();
  const value = intl.formatMessage({ id: `tasktable.header.spotlight.priority.${row.priority}` }).toUpperCase();
  return (<TasksTableCell id={row.id + "/Priority"} name={value} />);
}
const Subject: React.FC<CellProps> = ({ row, maxWidth }) => {
  return (<TasksTableCell id={row.id + "/Subject"} name={row.title} maxWidth={maxWidth}/>);
}

export type { CellProps }
export { Subject, Priority, Status, Owners, DueDate, Roles, Desc };

