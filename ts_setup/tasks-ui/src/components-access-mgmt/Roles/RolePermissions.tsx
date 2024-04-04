import React from 'react';
import { Stack, Divider, Alert } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { Role } from 'descriptor-access-mgmt';
import { SectionLayout } from 'components-generic';
import Context from 'context';

const RolePermissions: React.FC<{ role: Role }> = ({ role }) => {
  const { permissions } = Context.useAccessMgmt()


  if (!role.permissions.length) {
    return (<Alert severity='info'><FormattedMessage id='permissions.permission.none' /></Alert>)
  }

  return (
    <Stack spacing={1}>
      {permissions.map((permission, index) => (
        <React.Fragment key={index}>
          <SectionLayout label='permissions.permission.name' value={permission.name} />
          <SectionLayout label='permissions.permission.description' value={permission.description} />
          <SectionLayout label='permissions.permission.status' value={permission.status} />
          {role.permissions.length - 1 !== index ? <Divider /> : undefined}
        </React.Fragment>
      ))
      }
    </Stack>
  )
}

export { RolePermissions };