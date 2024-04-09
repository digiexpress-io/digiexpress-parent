import React from 'react';
import { Alert, AlertTitle, Box, Chip, Stack } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { FilterByString, LayoutListItem } from 'components-generic';
import { useAm } from 'descriptor-access-mgmt';

import { useNewRole, useTabs } from './RoleCreateContext';


const CurrentlySelected: React.FC<{ chips: string[], onRemoveChip: (index: number) => void }> = ({ chips, onRemoveChip }) => {
  return (
    <Alert severity='info' icon={false}>
      <AlertTitle><FormattedMessage id='permissions.select.currentSelection' /></AlertTitle>
      {chips.length ? chips.map((label, index) => (
        <Chip
          sx={{ m: '2px' }}
          label={label}
          key={index}
          onDelete={() => onRemoveChip(index)}
        />
      )) : <FormattedMessage id='permissions.role.roleCreate.selection.none' />}
    </Alert>
  );
};

const RoleParent: React.FC = () => {
  const { roles, getRole } = useAm();
  const { setParentId, entity } = useNewRole();
  const parentRole = entity.parentId ? getRole(entity.parentId) : undefined;
  return (
    <>
      <Stack spacing={1}>
        <CurrentlySelected chips={parentRole ? [parentRole.name] : []} onRemoveChip={() => setParentId(undefined)} />
        <FilterByString onChange={() => { }} />
      </Stack>
      <Box sx={{ mt: 1 }}>
        {roles.map((role, index) => <LayoutListItem key={role.id}
          index={index}
          active={role.id === parentRole?.id}
          onClick={() => setParentId(role.id)}>
          {role.name}
        </LayoutListItem>
        )}
      </Box>
    </>
  )
}


const RolePermissions: React.FC = () => {
  const { permissions } = useAm();
  const { addPermission, removePermission, entity } = useNewRole();


  if (!permissions) {
    console.log('no permissions found')
  }

  function handlePermission(permission: string) {
    if (entity.permissions.includes(permission)) {
      removePermission(permission);
    }
    else { addPermission(permission) };
  }

  return (<>
    <Stack spacing={1}>
      <CurrentlySelected chips={entity.permissions ? [...entity.permissions] : []} onRemoveChip={(index) => removePermission(entity.permissions[index])} />
      <FilterByString onChange={() => { }} />
    </Stack>

    <Box sx={{ mt: 1 }}>
      {permissions.map((permission, index) => <LayoutListItem key={permission.id}
        index={index}
        active={entity.permissions.includes(permission.name)}
        onClick={() => handlePermission(permission.name)}>
        {permission.name}
      </LayoutListItem>
      )}
    </Box>
  </>
  )
}

const RolePrincipals: React.FC = () => {
  const { principals } = useAm();
  const { addPrincipal, removePrincipal, entity } = useNewRole();

  if (!principals) {
    console.log('no principals found')
  }

  function handlePrincipal(principal: string) {
    if (entity.principals.includes(principal)) {
      removePrincipal(principal);
    }
    else { addPrincipal(principal) };
  }

  return (<>

    <Stack spacing={1}>
      <CurrentlySelected chips={entity.principals ? [...entity.principals] : []} onRemoveChip={(index) => removePrincipal(entity.principals[index])} />
      <FilterByString onChange={() => { }} />
    </Stack>

    <Box sx={{ mt: 1 }}>
      {principals.map((principal, index) => <LayoutListItem key={principal.id}
        index={index}
        active={entity.principals.includes(principal.name)}
        onClick={() => handlePrincipal(principal.name)}>
        {principal.name}
      </LayoutListItem>
      )}
    </Box>

  </>
  )
}

export const Right: React.FC = () => {
  const tabbing = useTabs();

  if (tabbing.activeTab.id === 'role_parent') {
    return (<RoleParent />);
  } else if (tabbing.activeTab.id === 'role_permissions') {
    return (<RolePermissions />);
  } else if (tabbing.activeTab.id === 'role_members') {
    return (<RolePrincipals />);
  } else return (<>unknown tab type</>
  )
}
