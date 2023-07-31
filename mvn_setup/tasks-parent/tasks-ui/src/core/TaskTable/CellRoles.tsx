import React from 'react';
import { Box, Avatar, SxProps } from '@mui/material';

import client from '@taskclient';
import Styles from '@styles';
import TaskCell from './TaskCell';
import { usePopover } from './CellPopover';
import { CellProps } from './task-table-types';


function getRoles(def: client.Group): SxProps | undefined {
  if (!def.color) {
    return undefined;
  }

  return undefined;
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

  return (<TaskCell id={row.id + "/Roles"} name={<Box flexDirection="row" display="flex">{avatars}</Box>} />);
}


const FormattedCell: React.FC<{
  rowId: number,
  row: client.TaskDescriptor,
  def: client.Group
}> = ({ row, def }) => {

  return (
    <Styles.TableCell sx={getRoles(def)}><Roles row={row} def={def}/></Styles.TableCell>
  );
}

export default FormattedCell;


