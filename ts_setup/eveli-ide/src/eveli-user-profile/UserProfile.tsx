import React from 'react';
import { Typography, CircularProgress, Divider, Grid2 } from '@mui/material';
import PersonIcon from '@mui/icons-material/Person';
import EditIcon from '@mui/icons-material/Edit';
import NotificationsIcon from '@mui/icons-material/Notifications';
import { FormattedMessage } from 'react-intl';
import { DateTime } from 'luxon';

import { UserAvatar } from './UserAvatar';
import { FirstName, LastName, NotificationSettings } from './UserProfileEditFields';
import { SectionRow } from '@/eveli-styles';
import { PrefsApi } from '@/api-prefs';
import { useFetch } from '@dxs-ts/eveli-fetch';
import { EveliUserOverviewDetail, EveliUserProfileRoot, EveliUserProfileHeader, useUtilityClasses } from './useUtilityClasses';


const formatFinnishDate = (isoString: string) =>
  DateTime.fromISO(isoString).setLocale('fi').toLocaleString(DateTime.DATE_SHORT);

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
    <EveliUserProfileHeader ownerState={state}>
      <Typography><FormattedMessage id='eveli.userProfile.title' /></Typography>
      <UserAvatar user={state} />
    </EveliUserProfileHeader>

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
              <SectionRow label={<FormattedMessage id='eveli.userProfile.displayName' />} value={state.details.username} />
              <SectionRow label={<FormattedMessage id='eveli.userProfile.email' />} value={state.details.email} />
              <SectionRow label={<FormattedMessage id='eveli.userProfile.created' />} value={formatFinnishDate(state.created)} />
              <SectionRow label={<FormattedMessage id='eveli.userProfile.updated' />} value={formatFinnishDate(state.updated)} />
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

      </Grid2>
    </EveliUserProfileRoot>

  </>
  );
}

