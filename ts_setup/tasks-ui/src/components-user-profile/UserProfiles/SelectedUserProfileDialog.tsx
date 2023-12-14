import React from 'react';
import { Grid, Stack, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { UserProfileAndOrg } from 'client';
import Context from 'context';

import Burger from 'components-burger';


const SectionLayout: React.FC<{ label: string, value: string | React.ReactNode | undefined }> = ({ label, value }) => {

  return (
    <Grid container>
      <Grid item md={4} lg={4}>
        <Typography fontWeight='400'><FormattedMessage id={label} /></Typography>
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

  return (<Burger.Dialog open={open} onClose={onClose} title='userProfile.frontoffice.title' backgroundColor='uiElements.main'>
    <Stack spacing={1}>
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
  </Burger.Dialog>
  );
}

export default SelectedUserProfileDialog;