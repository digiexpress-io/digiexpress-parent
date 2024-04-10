import React from 'react';
import { Typography, Grid } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import * as colors from 'components-colors';
import { LayoutList, NavigationButton, LayoutListItem, LayoutListFiller, FilterByString } from 'components-generic';
import { Permission, ImmutableAmStore } from 'descriptor-access-mgmt';
import Backend from 'descriptor-backend';
import { PermissionItem } from './PermissionItem';
import { PermissionCreateDialog } from 'components-access-mgmt/PermissionCreate';

const color_create_permission = colors.steelblue;

const PermissionsNavigation: React.FC<{}> = () => {
  //TODO
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

//TODO
const PermissionItems: React.FC = () => {
  return (<PermissionItem />)
}


const PermissionsOverviewLayout: React.FC = () => {
  const navigation = <PermissionsNavigation />;
  const pagination = <></>;
  const active = <div style={{ backgroundColor: 'pink' }}>ACTIVE</div>;
  const items = <PermissionItems />;

  return (<LayoutList slots={{ navigation, active, items, pagination }} />)
}

const PermissionsOverview: React.FC<{}> = () => {
  return (

    <PermissionsOverviewLayout />
  );
}

export { PermissionsOverview };

