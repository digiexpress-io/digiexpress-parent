import React from 'react';

import * as colors from 'components-colors';
import { LayoutList, NavigationButton, FilterByString } from 'components-generic';
import RoleCreateDialog from '../RoleCreate';
import { OneRoleData } from './OneRoleData';
import { PermissionsProvider, TabTypes, usePermissions } from '../PermissionsContext';
import { PermissionCreateDialog } from '../PermissionCreate';


const color_create_role = colors.cyan;
const color_create_permission = colors.steelblue;


const RoleNavigation: React.FC = () => {
  const [roleCreateOpen, setRoleCreateOpen] = React.useState(false);
  const [permissionCreateOpen, setPermissionCreateOpen] = React.useState(false);

  const { activeTab } = usePermissions();
  const { id } = activeTab;

  function getLocale(id: TabTypes) {
    return { count: 0 };
  }

  function handleSearch(value: React.ChangeEvent<HTMLInputElement>) {

  }

  function handleRoleCreate() {
    setRoleCreateOpen(prev => !prev);
  }

  function handlePermissionCreate() {
    setPermissionCreateOpen(prev => !prev);
  }

  return (<>
    <RoleCreateDialog open={roleCreateOpen} onClose={handleRoleCreate} />
    <PermissionCreateDialog open={permissionCreateOpen} onClose={handlePermissionCreate} />

    <FilterByString defaultValue={''} onChange={handleSearch} />
    <NavigationButton id='permissions.navButton.role.create'
      values={getLocale('role_create')}
      color={color_create_role}
      active={id === 'role_create'}
      onClick={handleRoleCreate} />
    <NavigationButton id='permissions.navButton.permission.create'
      values={getLocale('permission_create')}
      color={color_create_permission}
      active={id === 'permission_create'}
      onClick={handlePermissionCreate} />
  </>);
}



const RolesOverviewLayout: React.FC = () => {
  const { roles } = usePermissions();
  if (!roles) {
    return null;
  }

  const navigation = <RoleNavigation />;
  const pagination = <></>;
  const items = roles.map(role => (<OneRoleData role={role} key={role.id} />));
  return (<LayoutList slots={{ navigation, items, pagination }} />)
}


const RolesOverviewLoader: React.FC = () => {
  return (<PermissionsProvider><RolesOverviewLayout /></PermissionsProvider>);
}

export default RolesOverviewLoader;
