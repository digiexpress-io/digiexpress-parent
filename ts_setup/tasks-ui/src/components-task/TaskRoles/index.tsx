import React from 'react';
import { AvatarGroup, Box, Button, Avatar, List, MenuItem, Checkbox, ListItemText, Stack, Typography } from '@mui/material';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import { SearchFieldPopover } from '../SearchField';
import { useMockPopover } from '../TaskTable/MockPopover';
import { TaskDescriptor, AvatarCode } from 'descriptor-task';
import Client from 'client';
import Context from 'context';

const RoleAvatar: React.FC<{ children?: AvatarCode, onClick?: (event: React.MouseEvent<HTMLElement>) => void }> = ({ children, onClick }) => {
  const { state } = Context.useTasks();
  const roleColors = state.palette.roles;
  const bgcolor: string | undefined = children ? roleColors[children.value] : undefined;
  const avatar = children ? children.twoletters : <AdminPanelSettingsIcon sx={{ fontSize: 15 }} />;

  return (
    <Avatar onClick={onClick}
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


const RoleAvatars: React.FC<{
  task: TaskDescriptor,
}> = ({ task }) => {

  return task.rolesAvatars.length ?
    <AvatarGroup spacing='medium' sx={{ cursor: 'pointer' }} >
      {task.rolesAvatars.map((role: AvatarCode) => (<RoleAvatar key={role.value}>{role}</RoleAvatar>))}
    </AvatarGroup> :
    <RoleAvatar />
}

const FullnamesAndAvatars: React.FC<{
  task: TaskDescriptor,
}> = ({ task }) => {
  const org = Context.useOrg();

  return task.rolesAvatars.length ?
    (<Stack spacing={1}>
      {task.rolesAvatars.map((role: AvatarCode) => (<Box key={role.value} display='flex' alignItems='center' sx={{ cursor: 'pointer' }}>
        <RoleAvatar key={role.value}>{role}</RoleAvatar>
        <Box pl={1}><Typography>{org.state.org.roles[role.value]?.displayName}</Typography></Box>
      </Box>))}
    </Stack>)
    :
    (<RoleAvatar />);
}


const TaskRoles: React.FC<{
  task: TaskDescriptor,
  onChange: (command: Client.AssignTaskRoles) => Promise<void>,
  fullnames?: boolean
}> = ({ task, onChange, fullnames }) => {
  const { state } = Context.useTasks();
  const roleColors = state.palette.roles;

  const [newRoles, setNewRoles] = React.useState(task.roles);
  const { setSearchString, searchResults } = Context.useRoles({ roles: newRoles });
  const Popover = useMockPopover();

  function handleToggleRole(role: Client.Role, currentlyChecked: boolean) {
    setNewRoles(currentListOfRoleIds => {
      const withoutCurrentRole = [...currentListOfRoleIds.filter((id) => id !== role.roleId)];

      // remove role
      if (currentlyChecked) {
        return withoutCurrentRole;
      }
      // add role
      return [...withoutCurrentRole, role.roleId];
    })
  }


  function onSubmit() {
    const isChanges = newRoles.sort().toString() !== task.roles.sort().toString();
    if (isChanges) {
      onChange({ roles: newRoles, commandType: 'AssignTaskRoles', taskId: task.id })
        .then(() => Popover.onClose());
      return;
    }
    Popover.onClose();
  }

  return (
    <Box>

      {fullnames ?
        (<Box onClick={Popover.onClick}><FullnamesAndAvatars task={task} /></Box>)
        :
        (<Button variant='text' color='inherit' sx={{ "&.MuiButtonBase-root": { minWidth: "unset" } }} onClick={Popover.onClick}>
          <RoleAvatars task={task} />
        </Button>)
      }


      <Popover.Delegate onClose={onSubmit}>
        <SearchFieldPopover onChange={setSearchString} />
        <List dense sx={{ py: 0 }}>
          {searchResults.map(({ role, checked }) => (
            <MenuItem key={role.roleId} sx={{ display: "flex", pl: 0, py: 0 }} onClick={() => handleToggleRole(role, checked)} >
              <Box sx={{ width: 8, height: 40, backgroundColor: roleColors[role.roleId] }} />
              <Box ml={1} >
                <Checkbox checked={checked} size='small' sx={{
                  height: "40px",
                  color: 'uiElements.main',
                  '&.Mui-checked': {
                    color: 'uiElements.main',
                  },
                }} />
              </Box>
              <ListItemText><Typography>{role.displayName}</Typography></ListItemText>
            </MenuItem>
          ))
          }
        </List>
      </Popover.Delegate>
    </Box>
  );
}

export default TaskRoles;