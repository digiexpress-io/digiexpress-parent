import React from 'react';
import { AvatarGroup, Box, ListItemText, Checkbox, Button, Avatar, List, MenuItem } from '@mui/material';
import PersonAddIcon from '@mui/icons-material/PersonAdd';

import Client from '@taskclient';

import SearchField from 'core/SearchField';
import { useMockPopover } from 'core/TaskTable/MockPopover';

const UserAvatar: React.FC<{ children?: Client.AvatarCode, onClick?: (event: React.MouseEvent<HTMLElement>) => void }> = ({ children, onClick }) => {
  const { state } = Client.useTasks();
  const assigneeColors = state.pallette.owners;
  const bgcolor: string | undefined = children ? assigneeColors[children.value] : undefined;
  const avatar = children ? children.twoletters : <PersonAddIcon sx={{ fontSize: 15 }} />;

  return (
    <Avatar
      onClick={onClick}
      sx={{
        bgcolor,
        width: 24,
        height: 24,
        fontSize: 10
      }}
    >
      {avatar}
    </Avatar>
  );
}

const TaskAssignees: React.FC<{ task: Client.TaskDescriptor }> = ({ task }) => {
  const Popover = useMockPopover();
  const { state } = Client.useTasks();
  const assigneeColors = state.pallette.owners;
  const { setSearchString, searchResults } = Client.useAssignees(task);

  const taskAssigneeAvatars = task.assigneesAvatars.length ?
    <AvatarGroup spacing='medium' onClick={Popover.onClick}>
      {task.assigneesAvatars.map((assignee: Client.AvatarCode) => (<UserAvatar key={assignee.value}>{assignee}</UserAvatar>))}
    </AvatarGroup> :
    <UserAvatar onClick={Popover.onClick} />

  return (
    <Box>
      <Button variant='text' color='inherit' sx={{ "&.MuiButtonBase-root": { minWidth: "unset" } }}>
        {taskAssigneeAvatars}
      </Button>
      <Popover.Delegate>
        <SearchField
          onChange={setSearchString}
        />
        <List dense sx={{ py: 0 }}>
          {searchResults.map(({ user, checked }) => (
            <MenuItem key={user.userId} sx={{ display: "flex", pl: 0, py: 0 }}>
              <Box sx={{ width: 8, height: 40, backgroundColor: assigneeColors[user.userId] }} />
              <Box ml={1}>
                <Checkbox checked={checked} sx={{ height: "40px" }} />
              </Box>
              <ListItemText>{user.displayName}</ListItemText>
            </MenuItem>
          ))}
        </List>
      </Popover.Delegate>
    </Box>
  );
}

export default TaskAssignees;