import React from 'react';
import { Grid, Stack, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import Context from 'context';
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
  const tenant = Context.useTenantConfig();
  const data = tenant.tenantConfig;

  console.log(data?.transactions)

  if (!data) {
    return (<>No data</>)
  }

  return (<Stack sx={{ px: 1 }} spacing={1}>
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
        {data.repoConfigs ? data.repoConfigs.map((repo) => (<>
          <SectionLayout label={`tenantConfig.frontoffice.${repo.repoType}`} value={repo.repoId} />
        </>
        )) : <FormattedMessage id='tenantConfig.frontoffice.noRepos' />}
      </>

    </Burger.Section>

  </Stack>);
}

export default CurrentTenant;