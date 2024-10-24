import React from 'react';
import { Grid, Stack, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { UserProfileDescriptor, ImmutableUserProfileDescriptor, ImmutableAmStore } from 'descriptor-access-mgmt';
import { NotificationSettings } from '../UserProfileEditFields';
import Backend from 'descriptor-backend';

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

const SelectedUserProfileDialog: React.FC<{
  open: boolean, 
  onClose: () => void, 
  profile: UserProfileDescriptor | undefined }> = 
  ({ profile, open, onClose }) => {

  const backend = Backend.useBackend();

  const [state, setState] = React.useState<UserProfileDescriptor | undefined>(profile);
  const [loading, setLoading] = React.useState<boolean>(false);

  React.useEffect(() => {
    new ImmutableAmStore(backend.store).currentUserProfile().then(userProfile => {
      setState(new ImmutableUserProfileDescriptor(userProfile.user));
      setLoading(false);
    });

  }, []);

  if (loading || !state) {
    return null;
  }

  return (<Burger.Dialog open={open} onClose={onClose} title='userProfile.frontoffice.title'>
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
      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='userProfile.frontoffice.notificationSettings' /></Typography>
        <NotificationSettings />
      </Burger.Section>
    </Stack>
  </Burger.Dialog>
  );
}

export default SelectedUserProfileDialog;