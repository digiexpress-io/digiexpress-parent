
import React from 'react';
import { Typography, Grid } from '@mui/material';
import { Permission } from 'descriptor-access-mgmt';

export const PermissionItem: React.FC<{ permission: Permission }> = ({ permission }) => {
  return (<>
    <Grid item sm={4} md={4} lg={4}>
      <Typography noWrap>{permission.name}</Typography>
    </Grid>

    <Grid item sm={4} md={4} lg={6}>
      <Typography noWrap>{permission.description}</Typography>
    </Grid>

    <Grid item sm={4} md={4} lg={2}>
      <Typography noWrap>{permission.status}</Typography>
    </Grid>
  </>)
}