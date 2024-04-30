import React from 'react';
import { AvatarGroup, Box, Button, Avatar as MAvatar, List, MenuItem, Checkbox, ListItemText, Stack, Typography, Alert, AlertTitle } from '@mui/material';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import { FormattedMessage } from 'react-intl';



import { cyan } from 'components-colors';
import { TaskDescriptor, AssignTaskRoles } from 'descriptor-task';
import { useAvatar } from 'descriptor-avatar';
import { Role, useTaskRoles, useRoleDisplayName } from 'descriptor-access-mgmt';

import { SearchFieldPopover } from '../SearchField';
import { TablePopover } from '../TablePopover';


const checkboxSx = { height: "40px", color: cyan, '&.Mui-checked': { color: cyan } };

const RoleBackgroundColor: React.FC<{ roleId: string }> = ({roleId}) => {
  const avatar = useAvatar(roleId);
  return (<Box sx={{ width: 8, height: 40, backgroundColor: avatar?.colorCode }} />);
}

export const SelectTaskRoles: React.FC<{
  anchorEl: HTMLElement | null,
  task: TaskDescriptor,
  onChange: (command: AssignTaskRoles) => Promise<void>,
  onClose: () => void,
}> = ({ task, onChange, onClose, anchorEl }) => {


  const [newRoles, setNewRoles] = React.useState(task.roles);
  const { setSearchString, searchResults } = useTaskRoles({ roles: newRoles });

  function handleToggleRole(role: Role, currentlyChecked: boolean) {
    setNewRoles(currentListOfRoleIds => {
      const withoutCurrentRole = [...currentListOfRoleIds.filter((id) => id !== role.id)];

      // remove role
      if (currentlyChecked) {
        return withoutCurrentRole;
      }
      // add role
      return [...withoutCurrentRole, role.id];
    })
  }


  function onSubmit() {
    const isChanges = newRoles.sort().toString() !== task.roles.sort().toString();
    if (isChanges) {
      onChange({ roles: newRoles, commandType: 'AssignTaskRoles', taskId: task.id }).then(onClose);
      return;
    }
    onClose();
  }

  return (
    <TablePopover onClose={onSubmit} anchorEl={anchorEl} open={true}>
      <SearchFieldPopover onChange={setSearchString} />
      <List dense sx={{ py: 0 }}>
      { searchResults.length ? searchResults.map(({ role, checked }) => 
        
        (<MenuItem key={role.id} sx={{ display: "flex", pl: 0, py: 0 }} onClick={() => handleToggleRole(role, checked)}>
            <RoleBackgroundColor roleId={role.id}/>
            <Box ml={1}><Checkbox checked={checked} size='small' sx={checkboxSx} /></Box>
            <ListItemText><Typography>{useRoleDisplayName(role)}</Typography></ListItemText>
          </MenuItem>)
        )
        
        :

        (<Box display='flex'>
          <Box sx={{ width: 8, backgroundColor: 'primary.main' }} />
          <Alert severity='info' sx={{ width: '100%' }}><AlertTitle><FormattedMessage id='search.results.none' /></AlertTitle></Alert>
        </Box>)
      }
      </List>
    </TablePopover>
  );
}

