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

function getAvatar(values: string[]): { twoletters: string, value: string }[] {
  return values.map(role => {
    const words: string[] = role.replaceAll("-", " ").replaceAll("_", " ").replace(/([A-Z])/g, ' $1').replaceAll("  ", " ").trim().split(" ");

    const result: string[] = [];
    for (const word of words) {
      if (result.length >= 2) {
        break;
      }

      if (word && word.length) {
        const firstLetter = word.substring(0, 1);
        result.push(firstLetter.toUpperCase());
      }
    }
    return { twoletters: result.join(""), value: role };
  });
}

const Roles: React.FC<CellProps> = ({ row, def }) => {
  const { state } = client.useTasks();

  const avatars = getAvatar(row.roles).map((entry, index) => <Avatar key={index} sx={{
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
  const avatars = getAvatar(row.owners).map((entry, index) => {
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
  const value = intl.formatMessage({ id: `tasktable.header.spotlight.status.${row.status}` });
  return (<TasksTableCell id={row.id + "/Status"} name={value} />);
}
const Priority: React.FC<CellProps> = ({ row }) => {
  return (<TasksTableCell id={row.id + "/Priority"} name={row.priority} />);
}
const Subject: React.FC<CellProps> = ({ row, maxWidth }) => {
  return (<TasksTableCell id={row.id + "/Subject"} name={row.subject} maxWidth={maxWidth}/>);
}

export type { CellProps }
export { Subject, Priority, Status, Owners, DueDate, Roles, Desc };

