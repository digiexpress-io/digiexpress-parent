import React from 'react';
import { Typography, Grid } from '@mui/material';

import * as colors from 'components-colors';
import { LayoutList, NavigationButton, LayoutListItem, FilterByString } from 'components-generic';
import { useAm } from 'descriptor-access-mgmt';
import PermissionItemActive from './PermissionItemActive';

import { PermissionsOverviewProvider, useActivePermission } from './PermissionsOverviewContext';

const color_create_permission = colors.steelblue;

const PermissionsNavigation: React.FC<{}> = () => {
  function handleSearch(value: React.ChangeEvent<HTMLInputElement>) { }

  return (<>
    <FilterByString onChange={handleSearch} />

    <NavigationButton id='permissions.navButton.permission.create'
      values={{}}
      color={color_create_permission}
      active={false}
      onClick={() => { }} />
  </>);
}

const PermissionItems: React.FC = () => {
  const { permissions } = useAm();
  const { setActivePermission, permissionId } = useActivePermission();

  if (!permissions) {
    return (<>no permissions defined</>);
  }


  return (<>
    {permissions.map((permission, index) => (
      <LayoutListItem active={permissionId === permission.id} index={index} key={permission.id} onClick={() => setActivePermission(permission.id)}>
        <Grid item sm={4} md={4} lg={4}>
          <Typography noWrap>{permission.name}</Typography>
        </Grid>

        <Grid item sm={4} md={4} lg={6}>
          <Typography noWrap>{permission.description}</Typography>
        </Grid>

        <Grid item sm={4} md={4} lg={2}>
          <Typography noWrap>{permission.status}</Typography>
        </Grid>
      </LayoutListItem>
    ))
    }
  </>
  )
}

const PermissionsOverviewLayout: React.FC = () => {
  const navigation = <PermissionsNavigation />;
  const pagination = <></>;
  const active = <PermissionItemActive />;
  const items = <PermissionItems />;

  return (<LayoutList slots={{ navigation, active, items, pagination }} />)
}

const PermissionsOverview: React.FC<{}> = () => {
  return (
    <PermissionsOverviewProvider>
      <PermissionsOverviewLayout />
    </PermissionsOverviewProvider>
  );
}

export { PermissionsOverview };

