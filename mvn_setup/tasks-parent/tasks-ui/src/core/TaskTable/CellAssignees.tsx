import React from 'react';
import { Avatar, AvatarGroup, Box, ListItemText, ListItem, Checkbox, Button, TextField, InputAdornment, List, ButtonProps, styled, ListItemTextProps } from '@mui/material';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import SearchIcon from '@mui/icons-material/Search';

import Client from '@taskclient';
import { usePopover } from './CellPopover';
import { StyledTableCell } from './StyledTable';

const StyledButton = styled(Button)<ButtonProps>(() => ({
  variant: 'text',
  color: 'inherit',
  "&.MuiButtonBase-root": {
    minWidth: "unset",
  },
}));

const StyledListItemText = styled(ListItemText)<ListItemTextProps>(({theme}) => ({
  marginLeft: theme.spacing(1)
}));

const UserAvatar: React.FC<{ children?: Client.AvatarCode, onClick?: (event: React.MouseEvent<HTMLElement>) => void }> = ({ children, onClick }) => {
  const { state } = Client.useTasks();
  const bgcolor: string | undefined = children ? state.pallette.owners[children.value] : undefined;

  return (<Avatar onClick={onClick}
    sx={{
      bgcolor,
      width: 24,
      height: 24,
      fontSize: 10,
    }}>

    {children ? children.twoletters : <PersonAddIcon sx={{ fontSize: 15 }} />}
  </Avatar>
  );
}

const SearchField: React.FC<{ onChange: (value: string) => void }> = ({ onChange }) => {

  return (<TextField
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
    onChange={(e) => onChange(e.target.value)}
  />);
}

const FormattedCell: React.FC<{
  rowId: number,
  row: Client.TaskDescriptor,
}> = ({ row }) => {

  const Popover = usePopover();
  const org = Client.useOrg();
  const [searchString, setSearchString] = React.useState<string>('');
  const { users } = org.state.org;

  const foundUsers = React.useMemo(() => {

    const result = searchString ?
      Object.values(users).filter(entry => entry.displayName.toLowerCase().includes(searchString.toLowerCase())) :
      Object.values(users);

    return result.map(user => ({

      checked: row.assignees.includes(user.userId),
      avatar: { twoletters: user.avatar, value: user.userId },
      user
    }));

  }, [row, users, searchString]);


  return (<StyledTableCell width="150px">
    <Box>
      <StyledButton>
        {row.assigneesAvatars.length ? (
          <AvatarGroup spacing='medium' onClick={Popover.onClick}>
            {row.assigneesAvatars.map(assignee => (<UserAvatar>{assignee}</UserAvatar>))}
          </AvatarGroup>) : <UserAvatar onClick={Popover.onClick}/>
        }
      </StyledButton>

      <Popover.Delegate>
        <SearchField onChange={setSearchString} />
        <List dense>
          {
            foundUsers.map(({ avatar, user, checked }) => (
              <ListItem key={user.userId}>
                <Checkbox checked={checked} />
                <UserAvatar>{avatar}</UserAvatar>
                <StyledListItemText>{user.displayName}</StyledListItemText>
              </ListItem>
            ))
          }
        </List>
      </Popover.Delegate>
    </Box>
  </StyledTableCell>);
}

export default FormattedCell;