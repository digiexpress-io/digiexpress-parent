import React from 'react';
import { Stack, Typography, Paper, CircularProgress } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';



import { UserAvatar } from './UserAvatar';
import { FirstName, LastName, EmailAddress, NotificationSettings } from './UserProfileEditFields';
import { Section, SectionRow } from '@/eveli-styles';
import { PrefsApi } from '@/api-prefs';
import { useFetch } from '@dxs-ts/eveli-fetch';


export const UserProfile: React.FC<{}> = () => {
  const intl = useIntl();
  const { restApi } = useFetch('worker/rest/api/userprofiles/$profileId.GET', {})
  const [state, setState] = React.useState<PrefsApi.UserProfile>();
  const [loading, setLoading] = React.useState<boolean>(true);

  React.useEffect(() => {
    restApi().currentUserProfile().then(userProfile => {
      alert('')
      setState(userProfile);
      setLoading(false);
    });
  }, []);

  if (loading || !state) {
    return <CircularProgress />;
  }

  const displayName = [state.details.lastName, state.details.lastName]
    .filter(e => !!e)
    .join(', ');

  return (<>
    <UserAvatar user={state} />

    <Paper sx={{ p: 1, height: '100%' }}>
      <Stack spacing={1}>

        <Section>
          <Typography fontWeight='bold'><FormattedMessage id='userProfile.frontoffice.info' /></Typography>
          <>
            <SectionRow label={<FormattedMessage id='userProfile.frontoffice.id'/>} value={state.id} />
            <SectionRow label={<FormattedMessage id='userProfile.frontoffice.displayName'/>} value={displayName} />
            <SectionRow label={<FormattedMessage id='userProfile.frontoffice.created'/>} value={new Date(state.created).toISOString()} />
            <SectionRow label={<FormattedMessage id='userProfile.frontoffice.updated'/>} value={new Date(state.updated).toISOString()} />
          </>
        </Section>

        <Section>
          <Typography fontWeight='bold'><FormattedMessage id='userProfile.frontoffice.firstName' /></Typography>
          <FirstName init={state} />
        </Section>
        <Section>
          <Typography fontWeight='bold'><FormattedMessage id='userProfile.frontoffice.lastName' /></Typography>
          <LastName init={state} />
        </Section>
        <Section>
          <Typography fontWeight='bold'><FormattedMessage id='userProfile.frontoffice.email' /></Typography>
          <EmailAddress init={state} />
        </Section>
        <Section>
          <Typography fontWeight='bold'><FormattedMessage id='userProfile.frontoffice.notificationSettings' /></Typography>
          <NotificationSettings />
        </Section>

        <Section>
          <Typography fontWeight='bold'><FormattedMessage id='userProfile.frontoffice.user.roles' /></Typography>
          <>
            ROLES TODO
          </>
        </Section>
      </Stack >
    </Paper >
  </>
  );
}
