import React from 'react';
import { AvatarGroup, Box, ListItemText, ListItem, AvatarProps, Checkbox, Button, Avatar, List, ButtonProps, styled, ListItemTextProps } from '@mui/material';
import PersonAddIcon from '@mui/icons-material/PersonAdd';

import Client from '@taskclient';

import { AvatarCode, TaskDescriptor } from 'taskclient/tasks-ctx-types';
import { usePopover } from 'core/TaskTable/CellPopover';
import { useAssignees } from 'taskclient/hooks';
import SearchField from 'core/SearchField';

const StyledButton = styled(Button)<ButtonProps>(() => ({
  variant: 'text',
  color: 'inherit',
  "&.MuiButtonBase-root": {
    minWidth: "unset",
  },
}));

const StyledAvatar = styled(Avatar)<AvatarProps & {bgcolor: string | undefined}>(({ bgcolor }) => ({
  backgroundColor: bgcolor,
  width: 24,
  height: 24,
  fontSize: 10
}));

const StyledListItemText = styled(ListItemText)<ListItemTextProps>(({theme}) => ({
  marginLeft: theme.spacing(1)
}));

const UserAvatar: React.FC<{ children?: Client.AvatarCode, onClick?: (event: React.MouseEvent<HTMLElement>) => void }> = ({ children, onClick }) => {
  const { state } = Client.useTasks();
  const bgcolor: string | undefined = children ? state.pallette.owners[children.value] : undefined;

  const avatar = children ? children.twoletters : <PersonAddIcon sx={{ fontSize: 15 }} />;

  return (
    <StyledAvatar onClick={onClick} bgcolor={bgcolor}>
      {avatar}
    </StyledAvatar>
  );
}
  
const TaskAssignees: React.FC<{ task: TaskDescriptor }> = ({ task }) => {
  const Popover = usePopover();
  const { setSearchString, searchResults } = useAssignees(task);

  const taskAssigneeAvatars = task.assigneesAvatars.length ? 
    <AvatarGroup spacing='medium' onClick={Popover.onClick}>
      {task.assigneesAvatars.map((assignee: AvatarCode) => (<UserAvatar key={assignee.value}>{assignee}</UserAvatar>))}
    </AvatarGroup> : 
    <UserAvatar onClick={Popover.onClick}/>

  return (
    <Box>
      <StyledButton>
        {taskAssigneeAvatars}
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

export default TaskAssignees;