import React from 'react';
import { Stack, Typography, Paper, CircularProgress } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { FirstName, LastName, EmailAddress, NotificationSettings } from './UserProfileEditFields';
import { UserProfileDescriptor, ImmutableUserProfileDescriptor } from 'descriptor-user-profile';
import { UserAvatar } from './UserAvatar';
import Context from 'context';

import { SectionLayout } from 'components-generic';
import Burger from 'components-burger';


const CurrentUserProfile: React.FC<{}> = () => {
  const backend = Context.useBackend();
  const [state, setState] = React.useState<UserProfileDescriptor>();
  const [loading, setLoading] = React.useState<boolean>(true);

  React.useEffect(() => {
    backend.currentUserProfile().then(userProfile => {
      setState(new ImmutableUserProfileDescriptor(userProfile.user));
      setLoading(false);
    });
  }, []);

  if (loading || !state) {
    return <CircularProgress />;
  }

  return (
    <Paper sx={{ p: 1, height: '100%' }}>
      <Stack spacing={1}>
        <UserAvatar user={state} />
        <Burger.Section>
          <Typography fontWeight='bold'><FormattedMessage id='userProfile.frontoffice.info' /></Typography>
          <>
            <SectionLayout label='userProfile.frontoffice.id' value={state.id} />
            <SectionLayout label='userProfile.frontoffice.displayName' value={state.displayName} />
            <SectionLayout label='userProfile.frontoffice.created' value={new Date(state.created).toISOString()} />
            <SectionLayout label='userProfile.frontoffice.updated' value={new Date(state.updated).toISOString()} />
            <SectionLayout label='userProfile.frontoffice.user.roles' value={"TODO"} />
          </>
        </Burger.Section>
        <Burger.Section>
          <Typography fontWeight='bold'><FormattedMessage id='userProfile.frontoffice.firstName' /></Typography>
          <FirstName init={state} />
        </Burger.Section>
        <Burger.Section>
          <Typography fontWeight='bold'><FormattedMessage id='userProfile.frontoffice.lastName' /></Typography>
          <LastName init={state} />
        </Burger.Section>
        <Burger.Section>
          <Typography fontWeight='bold'><FormattedMessage id='userProfile.frontoffice.email' /></Typography>
          <EmailAddress init={state} />
        </Burger.Section>
        <Burger.Section>
          <Typography fontWeight='bold'><FormattedMessage id='userProfile.frontoffice.notificationSettings' /></Typography>
          <NotificationSettings />
        </Burger.Section>
      </Stack >
    </Paper >
  );
}

export default CurrentUserProfile;