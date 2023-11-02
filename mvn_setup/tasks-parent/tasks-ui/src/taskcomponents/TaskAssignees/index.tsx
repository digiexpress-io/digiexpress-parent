import React from 'react';
import { AvatarGroup, Box, ListItemText, Checkbox, Button, Avatar, List, MenuItem, Stack, Typography } from '@mui/material';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import { SearchFieldPopover } from 'taskcomponents/SearchField';
import { useMockPopover } from 'taskcomponents/TaskTable/MockPopover';
import Client from 'client';
import { AvatarCode } from 'taskdescriptor';
import Context from 'context';

const UserAvatar: React.FC<{ children?: AvatarCode }> = ({ children }) => {
  const { state } = Context.useTasks();
  const assigneeColors = state.palette.owners;
  const bgcolor: string | undefined = children ? assigneeColors[children.value] : undefined;
  const avatar = children ? children.twoletters : <PersonAddIcon sx={{ fontSize: 15 }} />;


  return (
    <Avatar
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

const AvatarsOnly: React.FC<{
  task: {
    assignees: Client.UserId[],
    assigneesAvatars?: AvatarCode[]
  },
}> = ({ task }) => {

  return task.assignees.length ?
    (<AvatarGroup spacing='medium'>
      {(task.assigneesAvatars ?? Client.resolveAvatar(task.assignees)).map((assignee: AvatarCode) => (<UserAvatar key={assignee.value}>{assignee}</UserAvatar>))}
    </AvatarGroup>) :
    (<UserAvatar />)
}

const FullnamesAndAvatars: React.FC<{
  task: {
    assignees: Client.UserId[],
    assigneesAvatars?: AvatarCode[]
  },
}> = ({ task }) => {
  const org = Context.useOrg();

  return task.assignees.length ?
    (<Stack spacing={1}>
      {(task.assigneesAvatars ?? Client.resolveAvatar(task.assignees)).map((assignee: AvatarCode) => (<Box key={assignee.value} display='flex' alignItems='center' sx={{ cursor: 'pointer' }}>
        <UserAvatar key={assignee.value}>{assignee}</UserAvatar>
        <Box pl={1}><Typography>{org.state.org.users[assignee.value].displayName}</Typography></Box>
      </Box>))}
    </Stack>)
    :
    (<UserAvatar />);
}

const TaskAssignees: React.FC<{
  task: {
    assignees: Client.UserId[],
    assigneesAvatars?: AvatarCode[]
  },
  onChange: (assigneeIds: Client.UserId[]) => Promise<void>,
  fullnames?: boolean,
  disabled?: boolean,
}> = ({ task, onChange, fullnames, disabled }) => {

  const { state } = Context.useTasks();
  const assigneeColors = state.palette.owners;

  const Popover = useMockPopover();
  const [newAssignees, setNewAssignees] = React.useState(task.assignees);
  const { setSearchString, searchResults } = Context.useAssignees({ assignees: newAssignees });

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

  function onSubmit() {
    const isChanges = newAssignees.sort().toString() !== task.assignees.sort().toString();
    if (isChanges) {
      onChange(newAssignees)
        .then(() => Popover.onClose());
      return;
    }
    Popover.onClose();
  }

  return (
    <Box>
      {
        fullnames ?
          (<Box onClick={Popover.onClick}><FullnamesAndAvatars task={task} /></Box>)
          :
          (<Button disabled={disabled} variant='text' color='inherit' sx={{ "&.MuiButtonBase-root": { minWidth: "unset" } }} onClick={Popover.onClick}>
            <AvatarsOnly task={task} />
          </Button>)
      }


      <Popover.Delegate onClose={onSubmit}>
        <SearchFieldPopover onChange={setSearchString} />
        <List dense sx={{ py: 0 }}>
          {searchResults.map(({ user, checked }) => (
            <MenuItem key={user.userId} sx={{ display: "flex", pl: 0, py: 0 }} onClick={() => handleToggleUser(user, checked)}>
              <Box sx={{ width: 8, height: 40, backgroundColor: assigneeColors[user.userId] }} />
              <Box ml={1}>
                <Checkbox checked={checked} size='small'
                  sx={{
                    height: "40px",
                    color: 'uiElements.main',
                    '&.Mui-checked': {
                      color: 'uiElements.main',
                    },
                  }} />
              </Box>
              <ListItemText><Typography>{user.displayName}</Typography></ListItemText>
            </MenuItem>
          ))}
        </List>
      </Popover.Delegate>
    </Box >
  );
}
export default TaskAssignees;