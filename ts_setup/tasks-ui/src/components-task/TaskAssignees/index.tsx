import React from 'react';
import { AvatarGroup, Box, ListItemText, Checkbox, Button, List, MenuItem, Stack, Typography, Alert, AlertTitle } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import Client from 'client';
import Context from 'context';
import { cyan } from 'components-colors';
import { AvatarEmpty, AvatarUser, AvatarIndicator } from 'components-generic';


import { SearchFieldPopover } from '../SearchField';
import { TablePopover, usePopover } from '../TablePopover';


const AvatarsOnly: React.FC<{
  task: { assignees: Client.UserId[] },
}> = ({ task }) => {

  if(task.assignees.length > 0) {
    return (<AvatarGroup spacing='medium'>{task.assignees.map(assignee => (<AvatarUser key={assignee} children={assignee}/>))}</AvatarGroup>) 
  }
  return <AvatarEmpty />;
}

const FullnamesAndAvatars: React.FC<{
  task: { assignees: Client.UserId[] }
}> = ({ task }) => {

  if(task.assignees.length > 0) {
    return (<Stack spacing={1}>
      {task.assignees.map(assignee => (
        <Box key={assignee} display='flex' alignItems='center' sx={{ cursor: 'pointer' }}>
          <AvatarUser>{assignee}</AvatarUser>
          <Box pl={1}><Typography>{assignee}</Typography></Box>
        </Box>))
      }
    </Stack>);
  }

  return <AvatarEmpty />;
}

const SelectAssignees: React.FC<{
  anchorEl: HTMLElement | null,
  task: { assignees: Client.UserId[] },
  onChange: (assigneeIds: Client.UserId[]) => Promise<void>,
  onClose: () => void,
}> = ({ anchorEl, task, onChange, onClose }) => {
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

  function handleClose() {
    const isChanges = newAssignees.sort().toString() !== task.assignees.sort().toString();
    if (isChanges) {
      onChange(newAssignees).then(onClose);
      return;
    }
    onClose();
  }

  return (
    <TablePopover open={true} anchorEl={anchorEl} onClose={handleClose}>
      <SearchFieldPopover onChange={setSearchString} />
      <List dense sx={{ py: 0 }}>
        {searchResults.length ? searchResults.map(({ user, checked }) => (
          <MenuItem key={user.userId} sx={{ display: "flex", pl: 0, py: 0 }} onClick={() => handleToggleUser(user, checked)}>
            
            <AvatarIndicator userId={user.userId} />

            <Box ml={1}>
              <Checkbox checked={checked} size='small'
                sx={{
                  height: "40px",
                  color: cyan,
                  '&.Mui-checked': {
                    color: cyan,
                  },
                }} />
            </Box>
            <ListItemText><Typography>{user.displayName}</Typography></ListItemText>
          </MenuItem>
        )) : (
          <Box display='flex'>
            <Box sx={{ width: 8, backgroundColor: 'primary.main' }} />
            <Alert severity='info' sx={{ width: '100%' }}><AlertTitle><FormattedMessage id='search.results.none' /></AlertTitle></Alert>
          </Box>
        )
        }
      </List>
    </TablePopover>);
}


const TaskAssignees: React.FC<{
  task: { assignees: Client.UserId[] },
  onChange: (assigneeIds: Client.UserId[]) => Promise<void>,
  fullnames?: boolean,
  disabled?: boolean,
}> = ({ task, onChange, fullnames, disabled }) => {
  const { anchorEl, onClick, onClose, open } = usePopover();


  return (
    <Box>
      {
        fullnames ?
          (<Box onClick={onClick}><FullnamesAndAvatars task={task} /></Box>)
          :
          (<Button disabled={disabled} variant='text' color='inherit' sx={{ "&.MuiButtonBase-root": { minWidth: "unset" } }} onClick={onClick}>
            <AvatarsOnly task={task} />
          </Button>)
      }

      {open && <SelectAssignees anchorEl={anchorEl} onChange={onChange} onClose={onClose} task={task} />}
    </Box >
  );
}
export default TaskAssignees;