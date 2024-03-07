import React from 'react';

import * as colors from 'components-colors';
import { LayoutList, NavigationButton, FilterByString } from 'components-generic';

import { PermissionsProvider, TabTypes, usePermissions } from '../PermissionsContext';
import Fields from './RoleCreateFields';

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
  const { activeTab, roles } = usePermissions();

  if (!roles) { return <>no roles</> }
  if (activeTab.id === 'role_parent') { return (<Fields.RoleParent />) }
  if (activeTab.id === 'role_permissions') { return (<Fields.RolePermissions />) }

  if (activeTab.id === 'role_members') {
    return (<Fields.RolePrincipals />
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
