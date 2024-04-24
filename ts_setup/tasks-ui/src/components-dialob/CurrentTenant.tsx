import React from 'react';
import { Grid, Stack, Typography, Paper } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { useAm } from 'descriptor-access-mgmt';
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

const CurrentTenant: React.FC<{}> = () => {
  const am = useAm();
  const data = am.profile.tenant;

  if (!data) {
    return (<>No data</>)
  }

  return (
    <Paper sx={{ px: 1, height: '100%' }}>
      <Stack spacing={1}>
        <Typography variant='h3'><FormattedMessage id='tenantConfig.frontoffice.title' /></Typography>

        <Burger.Section>
          <Typography fontWeight='bold'><FormattedMessage id='tenantConfig.frontoffice.info' /></Typography>
          <>
            <SectionLayout label='tenantConfig.frontoffice.name' value={data.name} />
            <SectionLayout label='tenantConfig.frontoffice.id' value={data.id} />
            <SectionLayout label='tenantConfig.frontoffice.status' value={data.status} />
            <SectionLayout label='tenantConfig.frontoffice.documentType' value={data.documentType} />
            <SectionLayout label='tenantConfig.frontoffice.version' value={data.version} />
            <SectionLayout label='tenantConfig.frontoffice.created' value={data.created.toString()} />
            <SectionLayout label='tenantConfig.frontoffice.updated' value={data.updated.toString()} />
            {data.archived && <SectionLayout label='tenantConfig.frontoffice.archived' value={data.archived?.toString()} />}
          </>
        </Burger.Section>

        <Burger.Section>
          <Typography fontWeight='bold'><FormattedMessage id='tenantConfig.frontoffice.preferences' /></Typography>
          <SectionLayout label='tenantConfig.frontoffice.landingApp' value={data.preferences.landingApp} />
        </Burger.Section>

        <Burger.Section>
          <Typography fontWeight='bold'><FormattedMessage id='tenantConfig.frontoffice.repoConfigs' /></Typography>
          <>
            {data.repoConfigs ? data.repoConfigs.map((repo) => (<div key={repo.repoId}>
              <SectionLayout label={`tenantConfig.frontoffice.${repo.repoType}`} value={repo.repoId} />
            </div>
            )) : <FormattedMessage id='tenantConfig.frontoffice.noRepos' />}
          </>

        </Burger.Section>
      </Stack>
    </Paper>
  );
}

export default CurrentTenant;