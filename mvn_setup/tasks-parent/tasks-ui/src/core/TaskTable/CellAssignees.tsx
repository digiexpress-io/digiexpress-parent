import React from 'react';
import { Avatar, AvatarGroup, Box, SxProps, ListItemText, ListItem, Checkbox, Button, TextField, InputAdornment, List, ButtonProps } from '@mui/material';
import client from '@taskclient';
import { usePopover } from './CellPopover';
import { CellProps } from './task-table-types';
import { StyledTableCell } from './StyledTable';
import Client from '@taskclient';
import { AvatarCode } from 'taskclient/tasks-ctx-types';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import SearchIcon from '@mui/icons-material/Search';
import { styled } from "@mui/material/styles";

const StyledButton = styled(Button)<ButtonProps>(({ theme }) => ({
  "&.MuiButtonBase-root": {
    minWidth: "unset",
  },
}));

function getAssignees(def: client.Group): SxProps | undefined {
  if (!def.color) {
    return undefined;
  }
  return undefined;
}

const Assignees: React.FC<CellProps> = ({ row, def }) => {
  const { state } = client.useTasks();
  const Popover = usePopover();
  const org = Client.useOrg();
  const users = org.state.org.users;
  const assigneesAvatars = row.assigneesAvatars;
  const [searchString, setSearchString] = React.useState<string>('');

  const avatars = assigneesAvatars.map((entry, index) => {
    return (<Avatar key={index}
      sx={{
        bgcolor: state.pallette.owners[entry.value.toLowerCase()],
        width: 24,
        height: 24,
        fontSize: 10,
      }}>{entry.twoletters}</Avatar>
    );
  });

  avatars.push(<Avatar key='add-icon' sx={{ width: 24, height: 24, fontSize: 10 }}><PersonAddIcon sx={{ fontSize: 15 }} /></Avatar>)
  const avatarGroup = (avatars.length && <AvatarGroup spacing='medium' onClick={Popover.onClick}>{avatars}</AvatarGroup>);

  const userAvatarCodes: AvatarCode[] = users.map(({ displayName }) => ({
    value: displayName,
    twoletters: displayName.match(/\b\w/g)!.join(''),
  }));

  const filteredUserAvatarCodes = searchString !== '' ?
    userAvatarCodes.filter(entry => entry.value.toLowerCase().includes(searchString.toLowerCase())) :
    userAvatarCodes;

  const userAvatars = filteredUserAvatarCodes.map((entry, index) => {
    const value = entry.value.toLowerCase();
    return (
      <>
        <ListItem key={index}>
          <Checkbox checked={assigneesAvatars.find(a => a.value === value) !== undefined} />
          <Avatar key={index}
            sx={{
              bgcolor: state.pallette.owners[value.toLowerCase()],
              width: 24,
              height: 24,
              fontSize: 10,
              mr: 1,
            }}>{entry.twoletters}</Avatar>
          <ListItemText>{value}</ListItemText>
        </ListItem>
      </>
    );
  });

  return (
    <Box>
      <StyledButton variant='text' color='inherit'>
        {avatarGroup}
      </StyledButton>
      <Popover.Delegate>
        <TextField
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon color='primary' />
              </InputAdornment>
            ),
          }}
          fullWidth
          variant='standard'
          placeholder='Search'
          value={searchString}
          onChange={(e) => setSearchString(e.target.value)}
        />
        <List dense>
          {userAvatars}
        </List>
      </Popover.Delegate>
    </Box>
  )
}


const FormattedCell: React.FC<{
  rowId: number,
  row: client.TaskDescriptor,
  def: client.Group
}> = ({ row, def }) => {

  return (<StyledTableCell width="150px" sx={getAssignees(def)}><Assignees row={row} def={def} /></StyledTableCell>);
}

export default FormattedCell;

