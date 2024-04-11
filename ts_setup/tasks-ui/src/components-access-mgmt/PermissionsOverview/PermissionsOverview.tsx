import React from 'react';

import * as colors from 'components-colors';
import { LayoutList, NavigationButton, LayoutListItem, FilterByString } from 'components-generic';
import { useAm } from 'descriptor-access-mgmt';
import { PermissionItem } from './PermissionItem';
import PermissionItemActive from './PermissionItemActive';

import { PermissionsOverviewContextProvider, useActivePermission } from './PermissionsOverviewContext';

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
  const { setActive, permissionId } = useActivePermission();

  if (!permissions) {
    return (<>no permissions defined</>);
  }


  return (<>
    {permissions.map((permission, index) => (
      <LayoutListItem active={permissionId === permission.id} index={index} key={permission.id} onClick={() => setActive(permission.id)}>
        <PermissionItem key={permission.id} permission={permission} />
      </LayoutListItem>
    ))
    }
  </>
  )
}

const PermissionOverviewActive: React.FC = () => {
  return (<PermissionItemActive />);
}

const PermissionsOverviewLayout: React.FC = () => {
  const navigation = <PermissionsNavigation />;
  const pagination = <></>;
  const active = <PermissionOverviewActive />;
  const items = <PermissionItems />;

  return (<LayoutList slots={{ navigation, active, items, pagination }} />)
}

const PermissionsOverview: React.FC<{}> = () => {
  return (
    <PermissionsOverviewContextProvider>
      <PermissionsOverviewLayout />
    </PermissionsOverviewContextProvider>
  );
}

export { PermissionsOverview };

