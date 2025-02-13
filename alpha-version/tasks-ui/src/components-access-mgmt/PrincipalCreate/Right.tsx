import { useNewPrincipal, useTabs } from './PrincipalCreateContext';
import { Alert, AlertTitle, Box, Chip, Stack } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { FilterByString, LayoutListItem } from 'components-generic';
import { useAm } from 'descriptor-access-mgmt';
import React from 'react';


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
      )) : <FormattedMessage id='permissions.selection.none' />}
    </Alert>
  );
};

const PrincipalPermissions: React.FC = () => {
  const { permissions } = useAm();
  const { entity, addPermission, removePermission } = useNewPrincipal();


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

const PrincipalRoles: React.FC = () => {
  const { roles } = useAm();
  const { entity, addRole, removeRole } = useNewPrincipal();

  if (!roles) {
    console.log('no roles found')
  }

  function handleRole(role: string) {
    if (entity.roles.includes(role)) {
      removeRole(role);
    }
    else { addRole(role) };
  }

  return (<>
    <Stack spacing={1}>
      <CurrentlySelected chips={entity.roles ? [...entity.roles] : []} onRemoveChip={(index) => removeRole(entity.roles[index])} />
      <FilterByString onChange={() => { }} />
    </Stack>

    <Box sx={{ mt: 1 }}>
      {roles.map((role, index) => <LayoutListItem key={role.id}
        index={index}
        active={entity.roles.includes(role.name)}
        onClick={() => handleRole(role.name)}>
        {role.name}
      </LayoutListItem>
      )}
    </Box>
  </>
  )
}


export const Right: React.FC = () => {
  const tabbing = useTabs();


  if (tabbing.activeTab.id === 'principal_permissions') {
    return (<PrincipalPermissions />);
  } else if (tabbing.activeTab.id === 'principal_roles') {
    return (<PrincipalRoles />);
  } else return (<>unknown tab type</>);
}
