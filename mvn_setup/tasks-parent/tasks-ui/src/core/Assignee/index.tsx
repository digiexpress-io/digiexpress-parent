import React from 'react';
import { AvatarGroup, Box, ListItemText, InputAdornment, ListItem, Checkbox, Button, Avatar, List, TextField, ButtonProps, styled, ListItemTextProps } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import PersonAddIcon from '@mui/icons-material/PersonAdd';

import Client from '@taskclient';

import { TaskDescriptor } from 'taskclient/tasks-ctx-types';
import { usePopover } from 'core/TaskTable/CellPopover';
import { useAssignees } from 'taskclient/hooks';


export const StyledButton = styled(Button)<ButtonProps>(() => ({
  variant: 'text',
  color: 'inherit',
  "&.MuiButtonBase-root": {
    minWidth: "unset",
  },
}));

export const StyledListItemText = styled(ListItemText)<ListItemTextProps>(({theme}) => ({
  marginLeft: theme.spacing(1)
}));

const SearchField: React.FC<{ onChange: (value: string) => void }> = ({ onChange }) => {
  return (
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
      onChange={(e) => onChange(e.target.value)}
    />
  );
}

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
  
const Assignee: React.FC<{ task: TaskDescriptor }> = ({ task }) => {
  const Popover = usePopover();
  const { setSearchString, searchResults } = useAssignees(task);

  return (
    <Box>
      <StyledButton>
        {task.assigneesAvatars.length ? (
          <AvatarGroup spacing='medium' onClick={Popover.onClick}>
            {task.assigneesAvatars.map(assignee => (<UserAvatar>{assignee}</UserAvatar>))}
          </AvatarGroup>) : <UserAvatar onClick={Popover.onClick}/>
        }
      </StyledButton>

      <Popover.Delegate>
        <SearchField onChange={setSearchString} />
        <List dense>
          {
            searchResults.map(({ avatar, user, checked }) => (
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
  );
}

export default Assignee;