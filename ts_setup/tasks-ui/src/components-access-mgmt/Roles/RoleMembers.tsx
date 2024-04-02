import React from 'react';
import { Stack, Alert } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { Role } from 'descriptor-access-mgmt';
import { SectionLayout } from 'components-generic';

const RoleMembers: React.FC<{ role: Role }> = ({ role }) => {

  if (!role.principals.length) {
    return (<Alert severity='info'><FormattedMessage id='permissions.role.member.none' /></Alert>)
  }

  return (
    <Stack spacing={1}>
      {role.principals.map((principal) => (
        <SectionLayout label='permissions.role.member.name' value={principal.name} />
      ))
      }
    </Stack>
  )
}

export { RoleMembers };