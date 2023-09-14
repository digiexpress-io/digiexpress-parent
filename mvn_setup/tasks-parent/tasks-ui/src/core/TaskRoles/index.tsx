import React from 'react';
import { AvatarGroup, Box, ListItemText,AvatarProps, ListItem, Checkbox, Button, Avatar, List, ButtonProps, styled } from '@mui/material';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';

import Client from '@taskclient';

import { AvatarCode, TaskDescriptor } from 'taskclient/tasks-ctx-types';
import { usePopover } from 'core/TaskTable/CellPopover';
import { useRoles } from 'taskclient/hooks';
import SearchField from 'core/SearchField';

const StyledButton = styled(Button)<ButtonProps>(() => ({
  variant: 'text',
  color: 'inherit',
  "&.MuiButtonBase-root": {
    minWidth: "unset",
  },
}));

const StyledAvatar = styled(Avatar)<AvatarProps & {bgcolor: string | undefined}>(({ bgcolor, theme }) => ({
  marginRight: theme.spacing(1),
  backgroundColor: bgcolor,
  width: 24,
  height: 24,
  fontSize: 10
}));

const StyledAdminPanelSettingsIcon = styled(AdminPanelSettingsIcon)(() => ({
  fontSize: 15
}));

const RoleAvatar: React.FC<{ children?: Client.AvatarCode, onClick?: (event: React.MouseEvent<HTMLElement>) => void }> = ({ children, onClick }) => {
  const { state } = Client.useTasks();
  const bgcolor: string | undefined = children ? state.pallette.roles[children.value] : undefined;

  const avatar = children ? children.twoletters : <StyledAdminPanelSettingsIcon />;

  return (
    <StyledAvatar onClick={onClick} bgcolor={bgcolor}>
      {avatar}
    </StyledAvatar>
  );
}
  
const TaskRoles: React.FC<{ task: TaskDescriptor }> = ({ task }) => {
  const Popover = usePopover();
  const { setSearchString, searchResults } = useRoles(task);

  const taskRoleAvatars = task.rolesAvatars.length ? 
    <AvatarGroup spacing='medium' onClick={Popover.onClick}>
      {task.rolesAvatars.map((role: AvatarCode) => (<RoleAvatar key={role.value}>{role}</RoleAvatar>))}
    </AvatarGroup> : 
    <RoleAvatar onClick={Popover.onClick}/>;

  return (
    <Box>
      <StyledButton>
        {taskRoleAvatars}
      </StyledButton>
      <Popover.Delegate>
        <SearchField onChange={setSearchString} />
        <List dense>
          {
            searchResults.map(({ avatar, role, checked }) => (
              <ListItem key={role.roleId}>
                <Checkbox checked={checked} />
                <RoleAvatar>{avatar}</RoleAvatar>
                <ListItemText>{role.displayName}</ListItemText>
              </ListItem>
            ))
          }
        </List>
      </Popover.Delegate>
    </Box>
  );
}

export default TaskRoles;