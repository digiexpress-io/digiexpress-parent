import React from 'react';
import { Alert, AlertTitle, Box, Chip, Stack } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { FilterByString, LayoutListItem } from 'components-generic';
import { useNewPermission, useTabs } from './PermissionCreateContext';
import Context from 'context';

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
      )) : <FormattedMessage id='permissions.createRole.selection.none' />}
    </Alert>
  );
};

const PermissionRoles: React.FC = () => {
  const { roles } = Context.useAm();
  const { addRole, removeRole, entity } = useNewPermission();

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


const PermissionPrincipals: React.FC = () => {
  const { principals } = Context.useAm();
  const { addPrincipal, removePrincipal, entity } = useNewPermission();

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

  if (tabbing.activeTab.id === 'permission_roles') {
    return (<PermissionRoles />);
  } else if (tabbing.activeTab.id === 'permission_members') {
    return (<PermissionPrincipals />);
  } else return (<>unknown tab type</>
  )
}


