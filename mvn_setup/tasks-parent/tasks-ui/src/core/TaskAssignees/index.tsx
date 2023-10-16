import React from 'react';
import { AvatarGroup, Box, ListItemText, Checkbox, Button, Avatar, List, MenuItem } from '@mui/material';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import SearchField from 'core/SearchField';
import { useMockPopover } from 'core/TaskTable/MockPopover';
import Client from '@taskclient';

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

const TaskAssignees: React.FC<{ task: Client.TaskDescriptor, onChange: (command: Client.AssignTask) => Promise<void> }> = ({ task, onChange }) => {
  const { state } = Client.useTasks();
  const assigneeColors = state.pallette.owners;

  const Popover = useMockPopover();
  const [newAssignees, setNewAssignees] = React.useState(task.assignees);
  const { setSearchString, searchResults } = Client.useAssignees({ assignees: newAssignees });

  function handleToggleUser(user: Client.User, currentlyChecked: boolean) {
    setNewAssignees(currentListOfUserIds => {
      const withoutCurrentUser = [...currentListOfUserIds.filter(id => id !== user.userId)];

      // remove user
      if (currentlyChecked) {
        return withoutCurrentUser;
      }
      // add user
      return [...withoutCurrentUser, user.userId];
    });
  }

  const taskAssigneeAvatars = task.assigneesAvatars.length ?
    (<AvatarGroup spacing='medium' onClick={Popover.onClick}>
      {task.assigneesAvatars.map((assignee: Client.AvatarCode) => (<UserAvatar key={assignee.value}>{assignee}</UserAvatar>))}
    </AvatarGroup>) :
    (<UserAvatar />)

  function onSubmit() {
    const isChanges = newAssignees.sort().toString() !== task.assignees.sort().toString();
    if (isChanges) {
      onChange({ assigneeIds: newAssignees, commandType: 'AssignTask', taskId: task.id })
        .then(() => Popover.onClose());
      return;
    }
    Popover.onClose();
  }

  return (
    <Box>
      <Button variant='text' color='inherit' sx={{ "&.MuiButtonBase-root": { minWidth: "unset" } }}>
        {taskAssigneeAvatars}
      </Button>

      <Popover.Delegate onClose={onSubmit}>
        <SearchField onChange={setSearchString} />
        <List dense sx={{ py: 0 }}>
          {searchResults.map(({ user, checked }) => (
            <MenuItem key={user.userId} sx={{ display: "flex", pl: 0, py: 0 }}>
              <Box sx={{ width: 8, height: 40, backgroundColor: assigneeColors[user.userId] }} />
              <Box ml={1}>
                <Checkbox checked={checked} size='small' sx={{ height: "40px" }} onChange={() => handleToggleUser(user, checked)} />
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