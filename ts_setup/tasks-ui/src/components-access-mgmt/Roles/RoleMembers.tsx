import React from 'react';
import { Stack, Alert, Divider } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { Role } from 'descriptor-access-mgmt';
import { SectionLayout } from 'components-generic';
import { useAccessMgmt } from 'components-access-mgmt/AccessMgmtContext';

const MembersDivider: React.FC<{ index: number, role: Role }> = ({ index, role }) => {
  return role.principals.length - 1 !== index ? <Divider /> : null;
}


const OneRoleMembers: React.FC<{ principalName: string }> = ({ principalName }) => {
  const { getPrincipal } = useAccessMgmt();
  const { name, email, status } = getPrincipal(principalName);

  console.log('name', name)

  return (
    <>
      <SectionLayout label='role.principal.name' value={name} />
      <SectionLayout label='role.principal.email' value={email} />
      <SectionLayout label='role.principal.status' value={status} />
    </>
  )
}

const RoleMembers: React.FC<{ role: Role }> = ({ role }) => {

  if (!role.principals.length) {
    return (<Alert severity='info'><FormattedMessage id='permissions.role.member.none' /></Alert>)
  }

  console.log("principals", role.principals)
  return (
    <Stack spacing={1}>
      {role.principals.map((principalName, index) => (
        <>
          <OneRoleMembers key={principalName} principalName={principalName} />
          <MembersDivider index={index} role={role} />
        </>
      ))
      }
    </Stack>
  )
}

export { RoleMembers };