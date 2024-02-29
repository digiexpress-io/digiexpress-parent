import React from 'react';
import { Box, Stack, Divider } from '@mui/material';
import { Role } from 'descriptor-permissions';
import { SectionLayout } from 'components-generic';

const RolePermissions: React.FC<{ role: Role }> = ({ role }) => {

  return (
    <Box sx={{ p: 2, width: '100%', height: '100%' }}>
      {role.permissions.map((permission, index) => (
        <Stack spacing={1}>
          <SectionLayout label='permissions.permission.name' value={permission.name} />
          <SectionLayout label='permissions.permission.description' value={permission.description} />
          <SectionLayout label='permissions.permission.status' value={permission.status} />
          {role.permissions.length - 1 !== index ? <Divider /> : undefined}
        </Stack>
      ))
      }
    </Box>
  )
}

export { RolePermissions };