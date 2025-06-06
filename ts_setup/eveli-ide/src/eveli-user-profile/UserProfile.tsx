import React from 'react';
import { Typography, CircularProgress, Divider, Box, Grid2, ThemeProvider } from '@mui/material';
import PersonIcon from '@mui/icons-material/Person';
import EditIcon from '@mui/icons-material/Edit';
import NotificationsIcon from '@mui/icons-material/Notifications';
import { FormattedMessage } from 'react-intl';

import { UserAvatar } from './UserAvatar';
import { FirstName, LastName, EmailAddress, NotificationSettings } from './UserProfileEditFields';
import { SectionRow } from '@/eveli-styles';
import { PrefsApi } from '@/api-prefs';
import { useFetch } from '@dxs-ts/eveli-fetch';
import { EveliUserOverviewDetail, EveliUserProfileRoot, useUtilityClasses } from './useUtilityClasses';
import { UserActivity } from './UserActivity';



export const UserProfile: React.FC<{}> = () => {
  const { restApi } = useFetch('worker/rest/api/userprofiles/$profileId.GET', {})
  const [state, setState] = React.useState<PrefsApi.UserProfile>();
  const [loading, setLoading] = React.useState<boolean>(true);
  const classes = useUtilityClasses();

  React.useEffect(() => {
    restApi().currentUserProfile().then(userProfile => {
      //alert('')
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
    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 30 }}>
      <Typography variant='h1'><FormattedMessage id='eveli.userProfile.title' /></Typography><UserAvatar user={state} />
    </div>


    <EveliUserProfileRoot className={classes.root}>
      <Grid2 container>
        <Grid2 size={{ md: 4, lg: 4, xl: 4 }}>
          <EveliUserOverviewDetail>
            <div className={classes.sectionTitle}>
              <PersonIcon />
              <Typography><FormattedMessage id='eveli.userProfile.currentDetails' /></Typography>
            </div>
            <Divider className={classes.divider} />


            <div style={{ marginTop: 10 }}>
              <SectionRow label={<FormattedMessage id='eveli.userProfile.id' />} value={state.id} />
              <SectionRow label={<FormattedMessage id='eveli.userProfile.displayName' />} value={displayName} />
              <SectionRow label={<FormattedMessage id='eveli.userProfile.created' />} value={new Date(state.created).toISOString()} />
              <SectionRow label={<FormattedMessage id='eveli.userProfile.updated' />} value={new Date(state.updated).toISOString()} />
              <SectionRow label={<FormattedMessage id='eveli.userProfile.userRoles' />} value='TODO' />
            </div>
          </EveliUserOverviewDetail>
        </Grid2>

        <Grid2 size={{ md: 4, lg: 4, xl: 4 }}>
          <EveliUserOverviewDetail>
            <div className={classes.sectionTitle}>
              <EditIcon />
              <Typography><FormattedMessage id='eveli.userProfile.editDetails' /></Typography>
            </div>

            <Divider className={classes.divider} />

            <FirstName init={state} />
            <LastName init={state} />
          <EmailAddress init={state} />
          </EveliUserOverviewDetail>
        </Grid2>

        <Grid2 size={{ md: 4, lg: 4, xl: 4 }}>
          <EveliUserOverviewDetail>
            <div className={classes.sectionTitle}>
              <NotificationsIcon />
              <Typography><FormattedMessage id='eveli.userProfile.notificationSettings' /></Typography>
            </div>
            <Divider className={classes.divider} />

          <NotificationSettings />
          </EveliUserOverviewDetail>
        </Grid2>

        <Grid2 size={{ md: 12, lg: 12, xl: 12 }}>
          <EveliUserOverviewDetail>
            <div className={classes.sectionTitle}>
              <NotificationsIcon />
              <Typography><FormattedMessage id='eveli.userProfile.userActivity' /></Typography>
            </div>
            <Divider className={classes.divider} />

            <UserActivity />
          </EveliUserOverviewDetail>
        </Grid2>

      </Grid2>
    </EveliUserProfileRoot>

  </>
  );
}
