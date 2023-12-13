import React from 'react';
import { Grid, Stack, Typography, Paper } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { FirstName, LastName, EmailAddress, NotificationSettings } from './UserProfileEditFields';
import Burger from 'components-burger';


const SectionLayout: React.FC<{ label: string, value: string | undefined }> = ({ label, value }) => {

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

const CurrentUserProfile: React.FC<{}> = () => {

  return (
    <Paper sx={{ px: 1, height: '100%' }}>
      <Stack spacing={1}>
        <Typography variant='h3'><FormattedMessage id='userProfile.frontoffice.title' /></Typography>
        <Burger.Section>
          <Typography fontWeight='bold'><FormattedMessage id='userProfile.frontoffice.info' /></Typography>
          <>
            <SectionLayout label='userProfile.frontoffice.username' value={"johnsmith03"} />
            <SectionLayout label='userProfile.frontoffice.created' value={new Date().toLocaleDateString()} />
            <SectionLayout label='userProfile.frontoffice.updated' value={new Date().toLocaleDateString()} />
          </>
        </Burger.Section>
        <Burger.Section>
          <Typography fontWeight='bold'><FormattedMessage id='userProfile.frontoffice.firstName' /></Typography>
          <FirstName />
        </Burger.Section>
        <Burger.Section>
          <Typography fontWeight='bold'><FormattedMessage id='userProfile.frontoffice.lastName' /></Typography>
          <LastName />
        </Burger.Section>
        <Burger.Section>
          <Typography fontWeight='bold'><FormattedMessage id='userProfile.frontoffice.email' /></Typography>
          <EmailAddress />
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