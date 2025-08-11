import React from 'react';
import { Typography, CircularProgress, Divider, Button, Box } from '@mui/material';
import PersonIcon from '@mui/icons-material/Person';
import EditIcon from '@mui/icons-material/Edit';
import SettingsIcon from '@mui/icons-material/Settings';

import { FormattedMessage, useIntl } from 'react-intl';
import { DateTime } from 'luxon';

import { UserAvatar } from './UserAvatar';
import { TenantConfigSelect } from './TenantConfigSelect';

import { UserProfileFirstName } from './UserProfileFirstName';
import { UserProfileLastName } from './UserProfileLastName';
import { UserProfileDetailRow } from './UserProfileDetailRow';

import { PrefsApi } from '@dxs-ts/eveli-api';
import { useIam } from '@dxs-ts/eveli-api';
import { useFetch } from '@dxs-ts/envir-fetch';
import { EveliUserOverviewDetail, EveliUserProfileRoot, EveliUserProfileHeader, useUtilityClasses } from './useUtilityClasses';
import { useQuery } from '@tanstack/react-query';


const formatFinnishDate = (isoString: string) =>
  DateTime.fromISO(isoString).setLocale('fi').toLocaleString(DateTime.DATE_SHORT);

export const UserProfile: React.FC<{}> = () => {
  const intl = useIntl();
  const { restApi } = useFetch('worker/rest/api/userprofiles/$profileId.GET', {})
  const { user } = useIam();
  const [changes, setChanges] = React.useState<Record<string, PrefsApi.UserProfileUpdateCommand<any>>>({});

  const { data: profile, error, refetch, isPending } = useQuery<PrefsApi.UserProfile>({
    queryKey: ['current-user-profile'],
    queryFn: () => restApi().currentUserProfile()
  });


  const userRoles = user.roles.length ? user.roles.join(", ") : intl.formatMessage({ id: 'eveli.noValueIndicator' });
  const userPermissions = user.permissions.length ? user.permissions.join(", ") : intl.formatMessage({ id: 'eveli.noValueIndicator' });
  const classes = useUtilityClasses();

  function handleChange(command: PrefsApi.UserProfileUpdateCommand<any>) {
    setChanges(prev => {
      const next: Record<string, PrefsApi.UserProfileUpdateCommand<any>> = { ...prev };
      const id: string = command.commandType;

      next[id] = command;
      return next;
    });
  }


  function handleSave() {
    restApi().updateUserProfile(Object.values(changes))
      .then(() => refetch())
      .then(() => setChanges({}));
  }


  if (isPending || !profile) {
    return <CircularProgress />;
  }

  return (<>
    <EveliUserProfileHeader ownerState={profile}>
      <Typography><FormattedMessage id='eveli.userProfile.title' /></Typography>
      <UserAvatar user={profile} />
    </EveliUserProfileHeader>

    <Box sx={{ justifySelf: 'center' }}>
      <EveliUserProfileRoot className={classes.root}>

        <EveliUserOverviewDetail>
          <div className={classes.sectionTitle}>
            <PersonIcon />
            <Typography><FormattedMessage id='eveli.userProfile.currentDetails' /></Typography>
          </div>
          <Divider className={classes.divider} />

          <div style={{ marginTop: 10 }}>
            <UserProfileDetailRow label={<FormattedMessage id='eveli.userProfile.id' />} value={profile.id} />
            <UserProfileDetailRow label={<FormattedMessage id='eveli.userProfile.displayName' />} value={profile.details.username} />
            <UserProfileDetailRow label={<FormattedMessage id='eveli.userProfile.firstAndLastName' />} value={profile.details.firstName + " " + profile.details.lastName} />
            <UserProfileDetailRow label={<FormattedMessage id='eveli.userProfile.email' />} value={profile.details.email} />
            <UserProfileDetailRow label={<FormattedMessage id='eveli.userProfile.created' />} value={formatFinnishDate(profile.created)} />
            <UserProfileDetailRow label={<FormattedMessage id='eveli.userProfile.updated' />} value={formatFinnishDate(profile.updated)} />
            <UserProfileDetailRow label={<FormattedMessage id='eveli.userProfile.userRoles' />} value={userRoles} />
            <UserProfileDetailRow label={<FormattedMessage id='eveli.userProfile.userPermissions' />} value={userPermissions} />
          </div>
        </EveliUserOverviewDetail>

        <EveliUserOverviewDetail>
          <div className={classes.sectionTitle}>
            <EditIcon />
            <Typography><FormattedMessage id='eveli.userProfile.editDetails' defaultMessage='User details' /></Typography>
          </div>

          <Divider className={classes.divider} />

          <Box display='flex' flexDirection='column'>
            <UserProfileFirstName init={profile} onChange={handleChange} />
            <UserProfileLastName init={profile} onChange={handleChange} />
          </Box>
        </EveliUserOverviewDetail>

        {/* //TODO
        <EveliUserOverviewDetail>
          <div className={classes.sectionTitle}>
            <NotificationsIcon />
            <Typography><FormattedMessage id='eveli.userProfile.notificationSettings' defaultMessage='Notification settings' /></Typography>
          </div>
          <Divider className={classes.divider} />

          <UserProfileNotifications />
        </EveliUserOverviewDetail>
         */}

        <EveliUserOverviewDetail>
          <div className={classes.sectionTitle}>
            <SettingsIcon />
            <Typography><FormattedMessage id='eveli.userProfile.tenantConfig' defaultMessage='Configuration options' /></Typography>
          </div>

          <Divider className={classes.divider} />

          <TenantConfigSelect userProfile={profile} onChange={handleChange} />
        </EveliUserOverviewDetail>

        <div style={{ display: 'flex', gap: 4, justifyContent: 'center' }}>
          <Button disabled={Object.values(changes).length === 0} onClick={handleSave}>{intl.formatMessage({ id: 'buttons.apply' })}</Button>
        </div>
      </EveliUserProfileRoot>
    </Box>
  </>
  );
}


