import React from 'react';
import { Box } from '@mui/material';

import * as colors from 'components-colors';
import { LayoutList, NavigationButton, FilterByString } from 'components-generic';

import { PermissionsProvider, TabTypes, usePermissions } from '../PermissionsContext';

const color_role_parent = colors.cyan;
const color_role_permission = colors.steelblue;
const color_role_members = colors.aquamarine;

const RoleCreateTabsNav: React.FC = () => {
  const { setActiveTab, activeTab } = usePermissions();
  const { id } = activeTab;

  function getLocale(id: TabTypes) {
    return { count: 0 };
  }

  function handleSearch(value: React.ChangeEvent<HTMLInputElement>) {

  }

  return (<>
    <FilterByString defaultValue={''} onChange={handleSearch} />
    <NavigationButton id='permissions.createRole.role_parent'
      values={getLocale('role_parent')}
      color={color_role_parent}
      active={id === 'role_parent'}
      onClick={() => setActiveTab("role_parent")} />

    <NavigationButton id='permissions.createRole.role_permissions'
      values={getLocale('role_permissions')}
      color={color_role_permission}
      active={id === 'role_permissions'}
      onClick={() => setActiveTab("role_permissions")} />

    <NavigationButton id='permissions.createRole.role_members'
      values={getLocale('role_members')}
      color={color_role_members}
      active={id === 'role_members'}
      onClick={() => setActiveTab("role_members")} />
  </>);
}


export const ActiveTabContent: React.FC = () => {
  const { activeTab, setActiveTab, roles } = usePermissions();

  React.useEffect(() => { setActiveTab('role_parent') }, [])

  if (!roles) {
    return <>no roles</>;
  }

  if (activeTab.id === 'role_parent') {
    return (<>
      {roles && roles.map((role) => (<Box>{role.name}</Box>))}
    </>
    )
  }

  if (activeTab.id === 'role_members') {
    return (<>
      {roles && roles.map((role) => (<>{role.principals.map((principal) => <Box>{principal.name}</Box>)}</>))}
    </>
    )
  }

  if (activeTab.id === 'role_permissions') {
    return (<>
      {roles && roles.map((role) => (<>{role.permissions.map((permission) => <Box>{permission.name}</Box>)}</>))}
    </>
    )
  }
  return <>nothing to see here</>
}

const RoleDialogRightLayout: React.FC = () => {
  const { roles } = usePermissions();

  if (!roles) {
    return <>no roles here on the right side!</>;
  }

  const navigation = <RoleCreateTabsNav />;
  const pagination = <></>;
  const items = <ActiveTabContent />;
  return (<LayoutList slots={{ navigation, items, pagination }} />)
}


const RoleCreateTabsNavLoader: React.FC = () => {
  return (<PermissionsProvider><RoleDialogRightLayout /></PermissionsProvider>);
}

export default RoleCreateTabsNavLoader;
