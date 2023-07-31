import React from 'react';
import { Avatar, AvatarGroup, Box, SxProps } from '@mui/material';

import client from '@taskclient';
import Styles from '@styles';

import TaskCell from './TaskCell';
import { usePopover } from './CellPopover';
import { CellProps } from './task-table-types';



function getAssignees(def: client.Group): SxProps | undefined {
  if (!def.color) {
    return undefined;
  }
  return undefined;
}

const Assignees: React.FC<CellProps> = ({ row, def }) => {
  const { state } = client.useTasks();
  const Popover = usePopover();

  const avatars = row.assigneesAvatars.map((entry, index) => {

    return (<Avatar key={index}
      sx={{
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

  const avatarGroup = (avatars.length && <AvatarGroup spacing='small' onClick={Popover.onClick}>{avatars}</AvatarGroup>);
  const name = (<Box flexDirection="row" display="flex">{avatarGroup}</Box>);

  return (
    <>
      <Popover.Delegate>
        <Box display='flex' flexDirection='column'>
          <Box>User 1</Box>
          <Box>User 2</Box>
          <Box>User 3</Box>
          <Box>User 4</Box>
          <Box>User 5</Box>
        </Box>
      </Popover.Delegate>
      <TaskCell id={row.id + "/Assignees"} name={name} />
    </>);
}


const FormattedCell: React.FC<{
  rowId: number,
  row: client.TaskDescriptor,
  def: client.Group
}> = ({ row, def }) => {

  return (<Styles.TableCell width="150px" sx={getAssignees(def)}><Assignees row={row} def={def} /></Styles.TableCell>);
}

export default FormattedCell;

