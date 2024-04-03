import React from 'react';
import { Alert, AlertTitle, Box, Chip, Button, Stack, Grid } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { FilterByString, LayoutListItem } from 'components-generic';
import { StyledRoleCreateTransferList } from './StyledRoleCreateTransferList';

import { useNewRole, useTabs } from './RoleCreateContext';
import Context from 'context';



const CurrentlySelected: React.FC<{ chips: string[], onRemoveChip: (index: number) => void }> = ({ chips, onRemoveChip }) => {
  return (
    <Alert severity='info' icon={false}>
      <AlertTitle><FormattedMessage id='permissions.select.currentSelection' /></AlertTitle>
      {chips.length ? chips.map((label, index) => (
        <Chip
          sx={{ mx: '2px' }}
          label={label}
          key={index}
          onDelete={() => onRemoveChip(index)}
        />
      )) : <FormattedMessage id='permissions.role.roleParentName.none' />}
    </Alert>
  );
};

const RoleParent: React.FC = () => {
  const { roles, getPermissionById } = Context.useAccessMgmt();
  const { setParentId, entity } = useNewRole();
  const parentRole = entity.parentId ? getPermissionById(entity.parentId) : undefined;
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
  const { permissions } = Context.useAccessMgmt();

  if (!permissions) {
    console.log('no permissions found')
  }

  function handleSave() {
    console.log('click for save!')
  }

  return (<>
    <FilterByString onChange={() => { }} />

    <StyledRoleCreateTransferList
      title="permissions.title"
      titleArgs={{ name: 'fun name' }}
      searchTitle="permissions.search.title"
      selectedTitle="permissions.selectedPermissions"
      headers={["permissions.permission.name", "permissions.permission.description"]}
      rows={permissions
        .sort((a, b) => a.name.localeCompare(b.name))
        .map((permission) => permission.id)}
      selected={permissions.map((r) => r.id)}
      filterRow={(row, search) => {
        const permission = permissions.find(permission => permission.id === row)?.name;
        return ((permission && permission.toLowerCase().indexOf(search) > -1) ? true : false);
      }}
      renderCells={(row) => {
        const { name, description } = permissions.find(permission => permission.id === row)!;
        return [name, description];
      }}
      cancel={{
        label: 'button.cancel',
        onClick: () => { }
      }}
      submit={{
        label: "button.apply",
        disabled: false,
        onClick: handleSave
      }}
    />
  </>
  )
}

const RolePrincipals: React.FC = () => {
  const { principals } = Context.useAccessMgmt();

  if (!principals) {
    console.log('no principals found')
  }

  function handleSave() {
    console.log('click for save!')
  }


  return (<>

    <FilterByString onChange={() => { }} />

    <StyledRoleCreateTransferList
      title="permissions.principals.title"
      titleArgs={{ name: 'fun name' }}
      searchTitle="permissions.principals.search.title"
      selectedTitle="permissions.principals.selectedPrincipals"
      headers={["permissions.principal.name"]}
      rows={principals
        .sort((a, b) => a.name.localeCompare(b.name))
        .filter((principal, index, srcArray) => srcArray.findIndex(p => p.id === principal.id) === index)
        .map((principal) => principal.id)
      }
      selected={principals
        .filter((principal, index, srcArray) => srcArray
          .findIndex(p => p.id === principal.id) === index)
        .map((r) => r.id)}
      filterRow={(row, search) => {
        const principal = principals.find(principal => principal.id === row)?.name;
        return ((principal && principal
          .toLowerCase()
          .indexOf(search) > -1) ? true : false);
      }}
      renderCells={(row) => {
        const { name } = principals.find(principal => principal.id === row)!;
        return [name];
      }}
      cancel={{
        label: 'button.cancel',
        onClick: () => { }
      }}
      submit={{
        label: "button.apply",
        disabled: false,
        onClick: handleSave
      }}
    />
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
