import React from 'react';
import { Stack, Grid } from '@mui/material';

import { NavigationSticky } from 'components-generic';
import { wash_me } from 'components-colors';

export const LayoutList: React.FC<{
  slots: {
    navigation: React.ReactNode;
    items: React.ReactNode | React.ReactNode[];
    pagination: React.ReactNode;
    active?: React.ReactNode | undefined;
  }
}> = ({ slots }) => {

  const {navigation, pagination, items, active} = slots;

  const size = active ? 8 : 12;

  return (<>
    { navigation ? <NavigationSticky>{navigation}</NavigationSticky> : null }

    <Grid container>
      <Grid item md={size} lg={size}>
        <Stack sx={{ backgroundColor: wash_me }}>{items}</Stack>
        {pagination}
      </Grid>

      { size !== 8 ? null :
        (<Grid item md={4} lg={4}>
          {active}
        </Grid>)
      }
    </Grid>
  </>
  );
}