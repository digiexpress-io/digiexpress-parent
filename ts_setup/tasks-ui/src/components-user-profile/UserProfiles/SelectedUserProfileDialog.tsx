import React from 'react';
import { Grid, Stack, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { UserProfileAndOrg } from 'client';
import { UserProfileDescriptor, UserProfileDescriptorImpl } from 'descriptor-user-profile';
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
  const tasks = Context.useTasks();
  const profile = tasks.state.profile;
  const [state, setState] = React.useState<UserProfileDescriptor>();
  const [loading, setLoading] = React.useState<boolean>(true);

  React.useEffect(() => {
    backend.currentUserProfile().then(userProfile => {
      setState(new UserProfileDescriptorImpl(userProfile.user, profile, new Date()));
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
          <SectionLayout label='userProfile.frontoffice.id' value={state.id} />
          <SectionLayout label='userProfile.frontoffice.displayName' value={state.displayName} />
          <SectionLayout label='userProfile.frontoffice.firstName' value={state.entry.details.firstName} />
          <SectionLayout label='userProfile.frontoffice.lastName' value={state.entry.details.lastName} />
          <SectionLayout label='userProfile.frontoffice.email' value={state.email} />
          <SectionLayout label='userProfile.frontoffice.created' value={<Burger.DateTimeFormatter type='dateTime' value={new Date(state.created)} />} />
          <SectionLayout label='userProfile.frontoffice.updated' value={<Burger.DateTimeFormatter type='dateTime' value={new Date(state.updated)} />} />
        </>
      </Burger.Section>

      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='userProfile.frontoffice.user.roles' /></Typography>
        <>
          ROLES TODO
        </>
      </Burger.Section>
    </Stack>
  </Burger.Dialog>
  );
}

export default SelectedUserProfileDialog;