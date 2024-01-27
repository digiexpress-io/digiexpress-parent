import React from 'react';
import { Stack, Grid, Typography, TablePagination, Alert } from '@mui/material';

import { FormattedMessage } from 'react-intl';

import { NavigationSticky, NavigationButton } from 'components-generic';
import { TaskDescriptor } from 'descriptor-task';
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
    <NavigationSticky>
      {navigation}
    </NavigationSticky>

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