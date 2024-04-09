import React from 'react';
import { Stack, Divider, Alert } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { Role } from 'descriptor-access-mgmt';
import { SectionLayout } from 'components-generic';
import Context from 'context';

const PermissionDivider: React.FC<{ index: number, role: Role }> = ({ index, role }) => {
  return role.permissions.length - 1 !== index ? <Divider /> : null;
}

const OneRolePermission: React.FC<{ permissionName: string }> = ({ permissionName }) => {
  const { getPermission } = Context.useAm()
  const { name, description, status } = getPermission(permissionName);

  return (
    <>
      <SectionLayout label='permissions.permission.name' value={name} />
      <SectionLayout label='permissions.permission.description' value={description} />
      <SectionLayout label='permissions.permission.status' value={status} />
    </>
  );
}


const RolePermissions: React.FC<{ role: Role }> = ({ role }) => {
  if (!role.permissions.length) {
    return (<Alert severity='info'><FormattedMessage id='permissions.permissionName.noneFound' /></Alert>)
  }

  return (
    <Stack spacing={1}>
      {role.permissions.map((permissionName, index) => (
        <>
          <OneRolePermission key={permissionName} permissionName={permissionName} />
          <PermissionDivider index={index} role={role} />
        </>
      ))}
    </Stack>
  )
}

export { RolePermissions };