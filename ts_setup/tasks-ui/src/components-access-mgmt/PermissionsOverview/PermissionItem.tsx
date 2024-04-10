
import React from 'react';
import { Typography, Grid } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { Permission } from 'descriptor-access-mgmt';

export const PermissionItem: React.FC<{ permission: Permission }> = ({ permission }) => {
  return (<>
    <Grid item lg={3}>
      <Typography><FormattedMessage id='permissions.permission.name' />{": "}{permission.name}</Typography>
    </Grid>

    <Grid item lg={6}>
      <Typography><FormattedMessage id='permissions.permission.description' />{": "}{permission.description}</Typography>
    </Grid>

    <Grid item lg={3}>
      <Typography><FormattedMessage id='permissions.permission.status' />{": "}{permission.status}</Typography>
    </Grid>
  </>)
}