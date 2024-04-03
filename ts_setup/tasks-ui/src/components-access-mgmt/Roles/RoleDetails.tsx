import React from 'react';
import { Stack } from '@mui/material';
import { Role } from 'descriptor-access-mgmt';
import { SectionLayout } from 'components-generic';

const RoleDetails: React.FC<{ role: Role }> = ({ role }) => {
  console.log('role', role)

  return (
    <Stack spacing={1}>
      <SectionLayout label='permissions.roles.description' value={role.description} />
      <SectionLayout label='permissions.roles.numberOfPermissions' value={role.permissions.length} />
      <SectionLayout label='permissions.roles.numberOfPrincpals' value={role.principals.length} />
      <SectionLayout label='permissions.roles.status' value={role.status} />
    </Stack>
  )
}

export { RoleDetails };