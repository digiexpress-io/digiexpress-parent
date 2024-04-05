import React from 'react';
import { Typography, Grid } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { ImmutableAccessMgmtStore, Principal } from 'descriptor-access-mgmt';
import Context from 'context';

const PrincipalsOverview: React.FC = () => {
  const backend = Context.useBackend();
  const [principals, setPrincipals] = React.useState<Principal[]>();

  React.useEffect(() => {
    new ImmutableAccessMgmtStore(backend.store).findAllPrincipals().then(setPrincipals);
  }, []);

  if (!principals) {
    return (<>no users defined</>);
  }

  return (<>
    {principals.sort((a, b) => a.name.localeCompare(b.name)).map((principal) => <Grid container display='flex' spacing={2} key={principal.id}>
      <Grid item lg={3}>
        <Typography><FormattedMessage id='permissions.principal.username' />{": "}{principal.name}</Typography>
      </Grid>

      <Grid item lg={7}>
        <Typography><FormattedMessage id='permissions.principal.email' />{": "}{principal.email}</Typography>
      </Grid>

      <Grid item lg={2}>
        <Typography><FormattedMessage id='permissions.principal.status' />{": "}{principal.status}</Typography>
      </Grid>

    </Grid>)}

  </>)
}

export { PrincipalsOverview };

