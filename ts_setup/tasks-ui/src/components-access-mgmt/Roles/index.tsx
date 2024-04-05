import React from 'react';

import * as colors from 'components-colors';
import { LayoutList, NavigationButton, FilterByString } from 'components-generic';
import RoleCreateDialog from '../RoleCreate';
import { OneRoleData } from './OneRoleData';
import { AccessMgmtContextProvider, useAccessMgmt } from '../AccessMgmtContext';
import { PermissionCreateDialog } from '../PermissionCreate';
import PrincipalCreateDialog from '../PrincipalCreate';

const color_create_role = colors.cyan;
const color_create_permission = colors.steelblue;
const color_create_principal = colors.cocktail_green;

const RoleNavigation: React.FC = () => {
  const [roleCreateOpen, setRoleCreateOpen] = React.useState(false);
  const [permissionCreateOpen, setPermissionCreateOpen] = React.useState(false);
  const [principalCreateOpen, setPrincipalCreateOpen] = React.useState(false);

  function handleSearch(value: React.ChangeEvent<HTMLInputElement>) {

  }

  function handleRoleCreate() {
    setRoleCreateOpen(prev => !prev);
  }

  function handlePermissionCreate() {
    setPermissionCreateOpen(prev => !prev);
  }

  function handlePrincipalCreate() {
    setPrincipalCreateOpen(prev => !prev);
  }

  return (<>
    <RoleCreateDialog open={roleCreateOpen} onClose={handleRoleCreate} />
    <PermissionCreateDialog open={permissionCreateOpen} onClose={handlePermissionCreate} />
    <PrincipalCreateDialog open={principalCreateOpen} onClose={handlePrincipalCreate} />

    <FilterByString defaultValue={''} onChange={handleSearch} />
    <NavigationButton id='permissions.navButton.role.create'
      values={{}}
      color={color_create_role}
      active={false}
      onClick={handleRoleCreate} />
    <NavigationButton id='permissions.navButton.permission.create'
      values={{}}
      color={color_create_permission}
      active={false}
      onClick={handlePermissionCreate} />
    <NavigationButton id='permissions.navButton.principal.create'
      values={{}}
      color={color_create_principal}
      active={false}
      onClick={handlePrincipalCreate} />
  </>);
}



const RolesOverviewLayout: React.FC = () => {
  const { roles } = useAccessMgmt();
  if (!roles) {
    return null;
  }

  const navigation = <RoleNavigation />;
  const pagination = <></>;
  const items = roles.map(role => (<OneRoleData role={role} key={role.id} />));
  return (<LayoutList slots={{ navigation, items, pagination }} />)
}


const RolesOverviewLoader: React.FC = () => {
  return (<AccessMgmtContextProvider><RolesOverviewLayout /></AccessMgmtContextProvider>);
}

export default RolesOverviewLoader;
