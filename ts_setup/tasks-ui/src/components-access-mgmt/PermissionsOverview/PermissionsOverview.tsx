import React from 'react';
import { Typography, Grid } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { Permission, ImmutableAmStore } from 'descriptor-access-mgmt';
import Backend from 'descriptor-backend';

const PermissionsOverview: React.FC = () => {
  const backend = Backend.useBackend();
  const [permissions, setPermissions] = React.useState<Permission[]>();


  React.useEffect(() => {
    new ImmutableAmStore(backend.store).findAllPermissions().then(setPermissions);
  }, []);

  if (!permissions) {
    return (<>no permissions defined</>);
  }

  return (<>
    {permissions.sort((a, b) => a.name.localeCompare(b.name)).map((permission) => <Grid container display='flex' spacing={2} key={permission.id}>
      <Grid item lg={3}>
        <Typography><FormattedMessage id='permissions.permission.name' />{": "}{permission.name}</Typography>
      </Grid>

      <Grid item lg={7}>
        <Typography><FormattedMessage id='permissions.permission.description' />{": "}{permission.description}</Typography>
      </Grid>

      <Grid item lg={2}>
        <Typography><FormattedMessage id='permissions.permission.status' />{": "}{permission.status}</Typography>
      </Grid>

    </Grid>)}

  </>)
}

export { PermissionsOverview };

