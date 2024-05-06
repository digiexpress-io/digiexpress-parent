import React from 'react';
import { Stack } from '@mui/material';
import { useIntl } from 'react-intl';
import { Role, useAm } from 'descriptor-access-mgmt';
import { SectionLayout } from 'components-generic';

const RoleDetails: React.FC<{ role: Role }> = ({ role }) => {
  const intl = useIntl();
  const { getRole } = useAm();

  const parentRole = role.parentId ? getRole(role.parentId) : undefined;

  return (
    <Stack spacing={1}>
      <SectionLayout label='permissions.roles.name' value={role.name} />

      {parentRole ? <SectionLayout label='permissions.roles.parent' value={parentRole.name} /> :
        <SectionLayout label='permissions.roles.parent' value={intl.formatMessage({ id: 'permissions.roles.parent.none' })} />
      }
      <SectionLayout label='permissions.roles.description' value={role.description} />
      <SectionLayout label='permissions.roles.numberOfPermissions' value={role.permissions.length} />
      <SectionLayout label='permissions.roles.numberOfPrincpals' value={role.principals.length} />
      <SectionLayout label='permissions.roles.status' value={role.status} />
    </Stack>
  )
}

export { RoleDetails };