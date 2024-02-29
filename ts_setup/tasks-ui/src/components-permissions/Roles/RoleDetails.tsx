import React from 'react';
import { Box, Stack } from '@mui/material';
import { Role } from 'descriptor-permissions';
import { SectionLayout } from 'components-generic';



const RoleDetails: React.FC<{ role: Role }> = ({ role }) => {

  return (
    <Box sx={{ p: 2, height: '100%', width: '100%' }}>
      <Stack spacing={1}>
        <SectionLayout label='permissions.roles.description' value={role.description} />
        <SectionLayout label='permissions.roles.numberOfPermissions' value={role.permissions.length} />
        <SectionLayout label='permissions.roles.numberOfPrincpals' value={role.principals.length} />
        <SectionLayout label='permissions.roles.status' value={role.status} />
      </Stack>
    </Box>
  )
}

export { RoleDetails };