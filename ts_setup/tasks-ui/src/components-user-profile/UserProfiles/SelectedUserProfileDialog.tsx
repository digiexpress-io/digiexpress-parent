import React from 'react';
import { Grid, Stack, Typography, Paper, Dialog } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { UserProfileAndOrg } from 'client';
import Context from 'context';

import Burger from 'components-burger';


const SectionLayout: React.FC<{ label: string, value: string | React.ReactNode | undefined }> = ({ label, value }) => {

  return (
    <Grid container>
      <Grid item md={4} lg={4}>
        <Typography fontWeight='bolder'><FormattedMessage id={label} /></Typography>
      </Grid>

      <Grid item md={8} lg={8}>
        <Typography>{value}</Typography>
      </Grid>

    </Grid>
  )
}

const SelectedUserProfileDialog: React.FC<{ open: boolean, onClose: () => void }> = ({ open, onClose }) => {
  const backend = Context.useBackend();
  const [state, setState] = React.useState<UserProfileAndOrg>();
  const [loading, setLoading] = React.useState<boolean>(true);

  React.useEffect(() => {
    backend.currentUserProfile().then(userProfile => {
      setState(userProfile);
      setLoading(false);
    });

  }, []);

  if (loading || !state) {
    return null;
  }

  return (<Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth >
    <Paper sx={{ p: 1 }}>
      <Stack spacing={1}>
        <Typography variant='h3'><FormattedMessage id='userProfile.frontoffice.title' /></Typography>
        <Burger.Section>
          <Typography fontWeight='bold'><FormattedMessage id='userProfile.frontoffice.info' /></Typography>
          <>
            <SectionLayout label='userProfile.frontoffice.id' value={state.user.id} />
            <SectionLayout label='userProfile.frontoffice.username' value={state.user.details.username} />
            <SectionLayout label='userProfile.frontoffice.created' value={<Burger.DateTimeFormatter type='dateTime' value={new Date(state.user.created)} />} />
            <SectionLayout label='userProfile.frontoffice.updated' value={<Burger.DateTimeFormatter type='dateTime' value={new Date(state.user.updated)} />} />
          </>
        </Burger.Section>
      </Stack>
    </Paper>
  </Dialog>
  );
}

export default SelectedUserProfileDialog;