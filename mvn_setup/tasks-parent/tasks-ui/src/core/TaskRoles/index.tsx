import React from 'react';
import { AvatarGroup, Box, Button, Avatar, List, MenuItem, Checkbox, ListItemText } from '@mui/material';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import SearchField from 'core/SearchField';
import { useMockPopover } from 'core/TaskTable/MockPopover';
import Client from '@taskclient';

const RoleAvatar: React.FC<{ children?: Client.AvatarCode, onClick?: (event: React.MouseEvent<HTMLElement>) => void }> = ({ children, onClick }) => {
  const { state } = Client.useTasks();
  const roleColors = state.pallette.roles;
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

const TaskRoles: React.FC<{ task: Client.TaskDescriptor, onChange: (command: Client.AssignTaskRoles) => Promise<void> }> = ({ task, onChange }) => {
  const { state } = Client.useTasks();
  const roleColors = state.pallette.roles;

  const [newRoles, setNewRoles] = React.useState(task.roles);
  const { setSearchString, searchResults } = Client.useRoles({ roles: newRoles });
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

  const taskRoleAvatars = task.rolesAvatars.length ?
    <AvatarGroup spacing='medium' onClick={Popover.onClick}>
      {task.rolesAvatars.map((role: Client.AvatarCode) => (<RoleAvatar key={role.value}>{role}</RoleAvatar>))}
    </AvatarGroup> :
    <RoleAvatar onClick={Popover.onClick} />;

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
      <Button variant='text' color='inherit' sx={{ "&.MuiButtonBase-root": { minWidth: "unset" } }}>
        {taskRoleAvatars}
      </Button>

      <Popover.Delegate onClose={onSubmit}>
        <SearchField onChange={setSearchString} />
        <List dense sx={{ py: 0 }}>
          {searchResults.map(({ role, checked }) => (
            <MenuItem key={role.roleId} sx={{ display: "flex", pl: 0, py: 0 }}>
              <Box sx={{ width: 8, height: 40, backgroundColor: roleColors[role.roleId] }} />
              <Box ml={1}>
                <Checkbox checked={checked} size='small' sx={{ height: "40px" }} onChange={() => handleToggleRole(role, checked)} />
              </Box>
              <ListItemText>{role.displayName}</ListItemText>
            </MenuItem>
          ))
          }
        </List>
      </Popover.Delegate>
    </Box>
  );
}

export default TaskRoles;