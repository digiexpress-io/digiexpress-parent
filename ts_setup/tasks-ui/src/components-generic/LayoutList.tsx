import React from 'react';
import { Stack, Grid } from '@mui/material';

import { NavigationSticky } from 'components-generic';
import { wash_me } from 'components-colors';

export const LayoutList: React.FC<{
  slots: {
    navigation: React.ReactNode;
    items: React.ReactNode;
    pagination: React.ReactNode;
    active: React.ReactNode;
  }
}> = ({ slots }) => {

  const {navigation, pagination, items, active} = slots;

  return (<>
    { navigation ? <NavigationSticky>{navigation}</NavigationSticky> : null }
    <Grid container>
      <Grid item md={8} lg={8}>
        <Stack sx={{ backgroundColor: wash_me }}>{items}</Stack>
        {pagination}
      </Grid>

      <Grid item md={4} lg={4}>
        {active}
      </Grid>
    </Grid>
  </>
  );
}