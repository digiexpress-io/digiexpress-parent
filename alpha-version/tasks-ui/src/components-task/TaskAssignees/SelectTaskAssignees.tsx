import React from 'react';
import { AvatarGroup, Box, ListItemText, Checkbox, Button, List, MenuItem, Stack, Typography, Alert, AlertTitle } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { cyan } from 'components-colors';
import { AvatarEmpty, AvatarUser, AvatarIndicator } from 'components-generic';
import { PrincipalId, useTaskAssignees, Principal, usePrincipalDisplayName } from 'descriptor-access-mgmt';

import { SearchFieldPopover } from '../SearchField';
import { TablePopover, usePopover } from '../TablePopover';


export const SelectTaskAssignees: React.FC<{
  anchorEl: HTMLElement | null,
  assigneeIds: PrincipalId[],
  onChange: (assigneeIds: PrincipalId[]) => Promise<void>,
  onClose: () => void,
}> = ({ anchorEl, assigneeIds, onChange, onClose }) => {

  
  const [newAssignees, setNewAssignees] = React.useState(assigneeIds);
  const { setSearchString, searchResults } = useTaskAssignees({ assignees: newAssignees });


  function handleToggleUser(user: Principal, currentlyChecked: boolean) {
    setNewAssignees(currentListOfUserIds => {
      const withoutCurrentUser = [...currentListOfUserIds.filter(id => id !== user.id)];

      // remove user
      if (currentlyChecked) {
        return withoutCurrentUser;
      }
      // add user
      return [...withoutCurrentUser, user.id];
    });
  }

  function handleClose() {
    const isChanges = newAssignees.sort().toString() !== assigneeIds.sort().toString();
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
          <MenuItem key={user.id} sx={{ display: "flex", pl: 0, py: 0 }} onClick={() => handleToggleUser(user, checked)}>
            
            <AvatarIndicator userId={user.id} />

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
            <ListItemText><Typography>{usePrincipalDisplayName(user)}</Typography></ListItemText>
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

